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

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import org.apache.felix.atomos.substrate.api.Launcher;
import org.apache.felix.atomos.substrate.api.plugin.FileCollectorPlugin;
import org.apache.felix.atomos.substrate.api.plugin.SubstratePlugin;
import org.osgi.util.converter.Converter;
import org.osgi.util.converter.Converters;

public class LauncherImpl implements Launcher
{

    private final List<SubstratePlugin<?>> plugins = new ArrayList<>();
    private Map<String, Object> config;

    private LauncherImpl()
    {
    }

    LauncherImpl(Collection<Class<? extends SubstratePlugin<?>>> pluginClasses, Map<String, Object> config)
    {
        this();
        this.config = Map.copyOf(config);//unmodifyable
        initPlugins(pluginClasses);

        execute();
    }

    public void execute()
    {
        //FileCollector
        //FileHandler
        //ClasspathHandler
        //JarHandler
        //JarEntryHandler
        //BundleActivatorHandler
        //SCDTO

        ContextImpl context = new ContextImpl();
        orderdPluginsBy(FileCollectorPlugin.class)//
        .forEachOrdered(p -> p.collectFiles(context));//

    }

    private void initPlugins(
        Collection<Class<? extends SubstratePlugin<?>>> pluginClasses)
    {
        Converter converter = Converters.standardConverter();
        pluginClasses.parallelStream()//
        .filter(Objects::nonNull)//
        .map(c -> {
            try
            {
                return c.getConstructor().newInstance();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            return null;
        })//
        .filter(Objects::nonNull)//
        .forEach(plugins::add);

        for (SubstratePlugin<?> plugin : plugins)
        {
            //find init Method
            Optional<Method> oMethod = Stream.of(plugin.getClass().getMethods())//
                .filter(m -> m.getName().equals("init"))//
                .filter(m -> m.getParameterCount() == 1)//
                .filter(m -> m.getParameterTypes()[0] != Object.class)//
                .findAny();
            // Convert config and init Plugin
            if (oMethod.isPresent())
            {
                Method method = oMethod.get();
                Object cfg = converter.convert(config).to(method.getParameterTypes()[0]);
                try
                {
                    method.invoke(plugin, cfg);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        }
    }

    private <T extends SubstratePlugin<?>> Stream<T> orderdPluginsBy(Class<T> clazz)
    {
        return plugins.parallelStream().filter(clazz::isInstance).map(clazz::cast).sorted(
            (p1, p2) -> p1.ranking(clazz) - p2.ranking(clazz));
    }

}