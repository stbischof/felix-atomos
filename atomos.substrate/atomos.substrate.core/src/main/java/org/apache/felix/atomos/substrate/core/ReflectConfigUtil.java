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

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.jar.JarFile;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.felix.atomos.substrate.api.reflect.ReflectConfig;
import org.apache.felix.atomos.substrate.core.reflect.ReflectConfigImpl;

public class ReflectConfigUtil
{
    private static String CLASS_START = "{\n";
    private static String CLASS_END = "}";
    private static String COMMA_SPACE = ", ";
    private static String COMMA_ENTER = ",\n";
    private static String NAME_PATTERN = "\"name\":\"%s\"";
    private static String CLASS_NAME = NAME_PATTERN;
    private static String FIELDS_START = "\"fields\" : [\n";
    private static String FIELD_NAME = NAME_PATTERN;
    private static String FIELDS_END = "]";
    private static String METHODS_START = "\"methods\" : [\n";
    private static String METHOD_NAME = NAME_PATTERN;
    private static String METHODS_END = FIELDS_END;
    private static String CONSTRUCTOR_METHOD_NAME = "<init>";

    private static String PARAMETER_TYPE = "\"parameterTypes\":[%s]";

    private static String COMPONENT_CONSTRUCTOR = "\"allPublicConstructors\" : true";
    private static String COMPONENT_METHODS = "\"allPublicMethods\" : true";
    private static String COMPONENT_FIELDS = "\"allPublicFields\" : true";

    public static List<Class<?>> loadClasses(List<Path> paths, URLClassLoader cl)
    {
        return paths.stream().map(p -> {
            try
            {
                return new JarFile(p.toFile());
            }
            catch (IOException e)
            {
                throw new UncheckedIOException(e);
            }
        }).flatMap(j -> j.stream()).filter(e -> !e.isDirectory()).filter(
            e -> e.getName().endsWith(".class")).filter(
                e -> !e.getName().endsWith("module-info.class")).map(e -> {
                    try
                    {
                        String name = e.getName().replace("/", ".").substring(0,
                            e.getName().length() - 6);
                        return cl.loadClass(name);
                    }
                    catch (NoClassDefFoundError | ClassNotFoundException e1)
                    {
                        //   happened when incomplete classpath
                    }
                    return null;
                }).collect(Collectors.toList());
    }

    public static String json(ReflectConfig reflectConfig)
    {
        StringBuilder builder = new StringBuilder();
        builder.append(ind(1)).append(CLASS_START);

        builder.append(ind(2)).append(
            String.format(CLASS_NAME, reflectConfig.getClassName()));

        AtomicReference<String> comma = new AtomicReference<>("");
        if (!reflectConfig.getFields().isEmpty())
        {
            builder.append(COMMA_ENTER).append(ind(2)).append(FIELDS_START);
            reflectConfig.getFields().stream().sorted(
                ReflectConfigImpl.fc).forEachOrdered(
                    f -> builder.append(comma.getAndSet(COMMA_ENTER)).append(
                        ind(3)).append("{").append(
                            String.format(FIELD_NAME, f.getFieldName())).append("}"));
            builder.append('\n').append(ind(2)).append(FIELDS_END);
        }

        comma.set("");
        if (!reflectConfig.getMethods().isEmpty()
            || !reflectConfig.getConstructors().isEmpty())
        {
            builder.append(COMMA_ENTER).append(ind(2)).append(METHODS_START);
            reflectConfig.getConstructors().stream().sorted(
                ReflectConfigImpl.cc).forEachOrdered(c -> {
                    builder.append(comma.getAndSet(COMMA_ENTER)).append(ind(3)).append(
                        "{").append(String.format(METHOD_NAME, CONSTRUCTOR_METHOD_NAME));

                    if (c.getMethodParameterTypes() != null)
                    {
                        String types = Stream.of(
                            c.getMethodParameterTypes()).sequential().collect(
                                Collectors.joining("\",\""));
                        if (!types.isEmpty())
                        {
                            types = "\"" + types + "\"";
                        }
                        builder.append(COMMA_SPACE).append(
                            String.format(PARAMETER_TYPE, types));
                    }
                    builder.append("}");
                });

            reflectConfig.getMethods().stream().sorted(
                ReflectConfigImpl.mc).forEachOrdered(m -> {
                    builder.append(comma.getAndSet(COMMA_ENTER))//
                    .append(ind(3)).append("{").append(
                        String.format(METHOD_NAME, m.getName()));
                    if (m.getMethodParameterTypes() != null)
                    {
                        String types = Stream.of(
                            m.getMethodParameterTypes()).sequential().collect(
                                Collectors.joining("\",\""));
                        if (!types.isEmpty())
                        {
                            types = "\"" + types + "\"";
                        }
                        builder.append(COMMA_SPACE).append(
                            String.format(PARAMETER_TYPE, types));
                    }
                    builder.append("}");
                });
            builder.append('\n').append(ind(2)).append(METHODS_END);
        }

        builder.append('\n').append(ind(1)).append(CLASS_END);

        return builder.toString();
    }

    private static Object ind(int num)
    {
        StringBuilder indent = new StringBuilder();
        for (int i = 0; i < num; i++)
        {
            indent.append("  ");
        }
        return indent.toString();
    }

    public static String json(List<ReflectConfigImpl> reflectConfigs)
    {
        StringBuilder builder = new StringBuilder();
        builder.append('[').append('\n');
        AtomicReference<String> comma = new AtomicReference<>("");

        Collections.sort(reflectConfigs, new Comparator<ReflectConfigImpl>()
        {
            @Override
            public int compare(ReflectConfigImpl o1, ReflectConfigImpl o2)
            {
                return o1.getClassName().compareTo(o2.getClassName());
            }
        });

        for (ReflectConfigImpl config : reflectConfigs)
        {
            builder.append(comma.getAndSet(COMMA_ENTER)).append(json(config));
        }
        builder.append('\n').append(']');
        return builder.toString();
    }
}
