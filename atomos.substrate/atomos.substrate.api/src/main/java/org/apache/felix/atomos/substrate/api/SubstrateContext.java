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
package org.apache.felix.atomos.substrate.api;

import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

public interface SubstrateContext
{

    default void addClass(Class<?> clazz)
    {
        addClass(clazz.getName());

    }

    void addClass(String className);

    default void addConstructor(Class<?> clazz, Class<?>[] parameterTypes)
    {
        String[] parameterTypeNames = null;
        if (parameterTypes != null)
        {
            parameterTypeNames = Stream.of(parameterTypes).map(Class::getName).toArray(
                String[]::new);
        }
        addConstructor(clazz, parameterTypeNames);
    }

    default void addConstructor(Class<?> bundleActivatorClass,
        String[] parameterTypeNames)
    {
        addConstructor(bundleActivatorClass.getName(), parameterTypeNames);
    }

    void addConstructor(String bundleActivatorClassName, String[] parameterTypeNames);

    default void addConstructorAllPublic(Class<?> clazz)
    {
        addConstructorAllPublic(clazz.getName());
    }

    void addConstructorAllPublic(String clazzName);

    default void addConstructorDefault(Class<?> clazz)
    {
        addConstructor(clazz, new Class[] {});

    }

    void addField(String fieldName, Class<?> clazz);

    default void addFieldsAllPublic(Class<?> clazz)
    {

        addFieldsAllPublic(clazz.getName());
    }

    void addFieldsAllPublic(String className);

    void addFile(Path path, FileType type);

    default void addMethod(Method method)
    {
        addMethod(method.getName(), method.getParameterTypes(),
            method.getDeclaringClass());
    }

    default void addMethod(String mName, Class<?> clazz)
    {
        addMethod(mName, null, clazz);
    }

    void addMethod(String methodName, Class<?>[] parameterTypes, Class<?> clazz);

    default void addMethodsAllPublic(Class<?> clazz)
    {

        addMethodsAllPublic(clazz.getName());
    }

    void addMethodsAllPublic(String clazz);

    void addRegisterServiceCalls(RegisterServiceCall registerServiceCall);

    Stream<Path> getFiles(FileType fileType);

    /**
     * @return
     */
    List<RegisterServiceCall> getRegisterServiceCalls();
}
