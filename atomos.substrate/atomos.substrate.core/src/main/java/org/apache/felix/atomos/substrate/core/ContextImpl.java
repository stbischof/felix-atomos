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
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Stream;

import org.apache.felix.atomos.substrate.api.FileType;
import org.apache.felix.atomos.substrate.api.RegisterServiceCall;
import org.apache.felix.atomos.substrate.api.SubstrateContext;
import org.apache.felix.atomos.substrate.api.reflect.ReflectConfig;
import org.apache.felix.atomos.substrate.core.reflect.ConstructorConfigImpl;
import org.apache.felix.atomos.substrate.core.reflect.FieldConfigImpl;
import org.apache.felix.atomos.substrate.core.reflect.MethodConfigImpl;
import org.apache.felix.atomos.substrate.core.reflect.ReflectConfigImpl;

public class ContextImpl implements SubstrateContext
{

    private final Map<Path, FileType> paths = new HashMap<>();

    private final Collection<ReflectConfigImpl> reflectConfigs = new ArrayList<>();

    private final List<RegisterServiceCall> registerServiceCalls = new ArrayList<>();
    @Override
    public void addClass(String className)
    {
        computeIfAbsent(className);
    }

    @Override
    public void addConstructor(String bundleActivatorClassName,
        String[] parameterTypeNames)
    {
        computeIfAbsent(bundleActivatorClassName).add(
            new ConstructorConfigImpl(parameterTypeNames));
    }

    @Override
    public void addConstructorAllPublic(String clazzName)
    {
        computeIfAbsent(clazzName).setAllPublicConstructors(true);
    }

    @Override
    public void addField(String fName, Class<?> clazz)
    {

        boolean exists = Stream.of(clazz.getDeclaredFields()).anyMatch(
            f -> f.getName().equals(fName));
        if (exists)
        {
            ReflectConfigImpl config = computeIfAbsent(clazz.getName());
            config.add(new FieldConfigImpl(fName));
        }

        Class<?> superClass = clazz.getSuperclass();
        if (superClass != null)
        {
            addField(fName, superClass);
        }

    }

    @Override
    public void addFieldsAllPublic(String className)
    {
        computeIfAbsent(className).setAllPublicFields(true);
    }

    @Override
    public void addFile(Path path, FileType type)
    {
        //maybe copy
        paths.put(path, type);
    }

    @Override
    //parameterTypes==null means not Set
    //parameterTypes=={} means no method parameter
    public void addMethod(String mName, Class<?>[] parameterTypes, Class<?> clazz)
    {
        for (Method m : clazz.getDeclaredMethods())
        {
            if (mName.equals(m.getName()))
            {
                if (parameterTypes == null)
                {
                    ReflectConfigImpl config = computeIfAbsent(clazz.getName());
                    config.add(new MethodConfigImpl(mName, null));
                    break;
                }
                else if (Arrays.equals(m.getParameterTypes(), parameterTypes))
                {
                    ReflectConfigImpl config = computeIfAbsent(clazz.getName());
                    String[] sParameterTypes = Stream.of(parameterTypes).sequential().map(
                        Class::getName).toArray(String[]::new);
                    config.add(new MethodConfigImpl(mName, sParameterTypes));
                    break;
                }
            }
        }
        Class<?> superClass = clazz.getSuperclass();
        if (superClass != null)
        {
            addMethod(mName, parameterTypes, superClass);
        }
    }

    @Override
    public void addMethodsAllPublic(String className)
    {
        computeIfAbsent(className).setAllPublicMethods(true);
    }

    @Override
    public void addRegisterServiceCalls(RegisterServiceCall registerServiceCall)
    {

        registerServiceCalls.add(registerServiceCall);




    }

    private ReflectConfigImpl computeIfAbsent(final String className)
    {
        Optional<ReflectConfigImpl> oConfig = reflectConfigs.stream().filter(
            c -> c.getClassName().equals(className)).findFirst();

        ReflectConfigImpl rc = null;
        if (oConfig.isPresent())
        {
            rc = oConfig.get();
        }
        else
        {
            rc = new ReflectConfigImpl(className);
            reflectConfigs.add(rc);
        }
        return rc;
    }

    @Override
    public Stream<Path> getFiles(FileType fileType)
    {
        return paths.entrySet().parallelStream().filter(
            e -> e.getValue().equals(fileType)).map(Entry::getKey);
    }

    Map<Path, FileType> getPaths()
    {
        return Map.copyOf(paths);
    }

    Collection<ReflectConfig> getReflectConfigs()
    {
        return List.copyOf(reflectConfigs);
    }

    @Override
    public List<RegisterServiceCall> getRegisterServiceCalls()
    {
        return registerServiceCalls;
    }

}
