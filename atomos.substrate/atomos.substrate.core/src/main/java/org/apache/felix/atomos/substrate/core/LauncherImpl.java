/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.felix.atomos.substrate.core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.StringTokenizer;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.felix.atomos.substrate.api.FileType;
import org.apache.felix.atomos.substrate.api.Launcher;
import org.apache.felix.atomos.substrate.api.RegisterServiceCall;
import org.apache.felix.atomos.substrate.api.SubstrateContext;
import org.apache.felix.atomos.substrate.api.plugin.BundleActivatorPlugin;
import org.apache.felix.atomos.substrate.api.plugin.ClassPlugin;
import org.apache.felix.atomos.substrate.api.plugin.ComponentDescription;
import org.apache.felix.atomos.substrate.api.plugin.ComponentMetaDataPlugin;
import org.apache.felix.atomos.substrate.api.plugin.FileCollectorPlugin;
import org.apache.felix.atomos.substrate.api.plugin.FileHandlerPlugin;
import org.apache.felix.atomos.substrate.api.plugin.JarPlugin;
import org.apache.felix.atomos.substrate.api.plugin.RegisterServicepPlugin;
import org.apache.felix.atomos.substrate.api.plugin.SubstratePlugin;
import org.apache.felix.atomos.substrate.core.scr.mock.EmptyBundeLogger;
import org.apache.felix.atomos.substrate.core.scr.mock.PathBundle;
import org.apache.felix.scr.impl.logger.BundleLogger;
import org.apache.felix.scr.impl.metadata.ComponentMetadata;
import org.apache.felix.scr.impl.parser.KXml2SAXParser;
import org.apache.felix.scr.impl.xml.XmlHandler;
import org.osgi.framework.Constants;

public class LauncherImpl implements Launcher
{

    List<SubstratePlugin<?>> getPlugins()
    {
        return List.copyOf(plugins);
    }

    private static List<ComponentDescription> readComponentDescription(JarFile jar)
        throws Exception
    {

        BundleLogger logger = new EmptyBundeLogger();
        List<ComponentMetadata> list = new ArrayList<>();
        Attributes attributes = jar.getManifest().getMainAttributes();
        String descriptorLocations = attributes.getValue("Service-Component");// ComponentConstants.SERVICE_COMPONENT);
        if (descriptorLocations != null)
        {
            StringTokenizer st = new StringTokenizer(descriptorLocations, ", ");
            while (st.hasMoreTokens())
            {
                String descriptorLocation = st.nextToken();
                InputStream stream = jar.getInputStream(jar.getEntry(descriptorLocation));
                BufferedReader in = new BufferedReader(
                    new InputStreamReader(stream, "UTF-8"));
                XmlHandler handler = new XmlHandler(new PathBundle(jar), logger, true,
                    true);

                KXml2SAXParser parser = new KXml2SAXParser(in);
                parser.parseXML(handler);
                list.addAll(handler.getComponentMetadataList());
            }

            //      //Felix SCR Version-> 2.1.17-SNAPSHOT
            //      //https://github.com/apache/felix-dev/commit/2d035e21d69c2bb8892d5d5d3e1027befcc3c50b#diff-dad1c7cc45e5c46bca969c95ac501546
            //      while (st.hasMoreTokens())
            //      {
            //          String descriptorLocation = st.nextToken();
            //          InputStream stream = jar.getInputStream(jar.getEntry(descriptorLocation));
            //          XmlHandler handler = new XmlHandler(new PathBundle(jar), logger, true, true);
            //
            //          final SAXParserFactory factory = SAXParserFactory.newInstance();
            //          factory.setNamespaceAware(true);
            //          final SAXParser parser = factory.newSAXParser();
            //          parser.parse( stream, handler );
            //          list.addAll(handler.getComponentMetadataList());
            //      }
        }

        List<ComponentDescription> cds = list.parallelStream().map(cmd -> {
            cmd.validate();
            return new ComponentDescriptionImpl(cmd);

        }).collect(Collectors.toList());
        return cds;
    }

    private Collection<SubstratePlugin<?>> plugins = null;

    private LauncherImpl()
    {

    }

    LauncherImpl(Collection<SubstratePlugin<?>> plugins)
    {

        this();
        this.plugins = plugins;

    }

    @Override
    public SubstrateContext execute()
    {
        return execute(new ContextImpl());
    }

    @Override
    public SubstrateContext execute(SubstrateContext context)
    {

        //CollectFiles
        orderdPluginsBy(FileCollectorPlugin.class)//
        .peek(System.out::println)//
        .forEachOrdered(plugin -> plugin.collectFiles(context));//

        //Visit all files with type
        orderdPluginsBy(FileHandlerPlugin.class)//
        .peek(System.out::println)//
        .forEachOrdered(plugin -> {

            context.getFiles(FileType.ARTEFACT)//
            .forEach(path -> plugin.handleFile(context, path, FileType.ARTEFACT));

            context.getFiles(FileType.CONFIG)//
            .forEach(path -> plugin.handleFile(context, path, FileType.CONFIG));

            context.getFiles(FileType.RESSOURCE)//
            .forEach(
                path -> plugin.handleFile(context, path, FileType.RESSOURCE));
        });//

        List<Path> artefacts = context.getFiles(FileType.ARTEFACT).collect(
            Collectors.toList());
        URL[] urls = artefacts.stream().map(p -> {
            try
            {
                return p.toUri().toURL();
            }
            catch (MalformedURLException e1)
            {
                throw new UncheckedIOException(e1);
            }
        }).toArray(URL[]::new);

        try (URLClassLoader classLoader = URLClassLoader.newInstance(urls, null))
        {

            List<Class<?>> classes = ReflectConfigUtil.loadClasses(artefacts,
                classLoader);

            orderdPluginsBy(ClassPlugin.class)//
            .peek(System.out::println)//
            .forEachOrdered(plugin -> {
                classes.forEach(c -> plugin.doClass(c, context));
            });

            for (Path path : artefacts)
            {
                JarFile jar = new JarFile(path.toFile());
                orderdPluginsBy(JarPlugin.class)//
                .peek(System.out::println)//
                .forEachOrdered(plugin -> plugin.doJar(jar, context, classLoader));

                Attributes attributes = jar.getManifest().getMainAttributes();
                String bundleActivatorClassName = attributes.getValue(
                    org.osgi.framework.Constants.BUNDLE_ACTIVATOR);
                if (bundleActivatorClassName == null)
                {
                    bundleActivatorClassName = attributes.getValue(
                        Constants.EXTENSION_BUNDLE_ACTIVATOR);
                }

                if (bundleActivatorClassName != null)
                {
                    Class<?> bundleActivatorClass;
                    try
                    {
                        bundleActivatorClass = classLoader.loadClass(
                            bundleActivatorClassName.trim());
                        orderdPluginsBy(BundleActivatorPlugin.class)//
                        .peek(System.out::println)//
                        .forEachOrdered(plugin -> plugin.doBundleActivator(
                            bundleActivatorClass, context, classLoader));
                    }
                    catch (ClassNotFoundException e)
                    {
                        e.printStackTrace();
                    }
                }
                //TODO add the from bundleAcrivator
                try
                {
                    List<ComponentDescription> cds = readComponentDescription(jar);
                    for (ComponentDescription cd : cds)
                    {
                        orderdPluginsBy(ComponentMetaDataPlugin.class)//
                        .peek(System.out::println)//
                        .forEachOrdered(plugin -> {

                            System.out.println(plugin);
                            System.out.println(cd);
                            plugin.doComponentMetaData(cd,

                                context, classLoader);
                        });
                    }
                }
                catch (Exception e)
                {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

                List<RegisterServiceCall> rscs = context.getRegisterServiceCalls();
                for (RegisterServiceCall rsc : rscs)
                {
                    orderdPluginsBy(RegisterServicepPlugin.class)//
                    .peek(System.out::println)//
                    .forEachOrdered(plugin -> {
                        plugin.doRegisterServiceCall(rsc, context, classLoader);
                    });
                }
            }
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return context;
    }

    private <T extends SubstratePlugin<?>> Stream<T> orderdPluginsBy(Class<T> clazz)
    {
        return plugins.parallelStream().filter(clazz::isInstance).map(clazz::cast).sorted(
            (p1, p2) -> p1.ranking(clazz) - p2.ranking(clazz));
    }
}