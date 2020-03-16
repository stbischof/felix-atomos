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

import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import org.apache.felix.atomos.substrate.api.FileType;
import org.apache.felix.atomos.substrate.api.RegisterServiceCall;
import org.apache.felix.atomos.substrate.api.SubstrateContext;

public interface TestContext extends SubstrateContext
{


    @Override
    default void addClass(String className)
    {

    }

    @Override
    default void addConstructor(String bundleActivatorClassName,
        String[] parameterTypeNames)
    {

    }

    @Override
    default void addConstructorAllPublic(String clazzName)
    {

    }

    @Override
    default void addField(String fieldName, Class<?> clazz)
    {

    }

    @Override
    default void addFieldsAllPublic(String className)
    {

    }

    @Override
    default void addFile(Path path, FileType type)
    {

    }

    @Override
    default void addMethod(String methodName, Class<?>[] parameterTypes, Class<?> clazz)
    {

    }

    @Override
    default void addMethodsAllPublic(String clazz)
    {

    }

    @Override
    default void addRegisterServiceCalls(RegisterServiceCall registerServiceCall)
    {

    }

    @Override
    default Stream<Path> getFiles(FileType fileType)
    {
        return null;
    }

    @Override
    default List<RegisterServiceCall> getRegisterServiceCalls()
    {

        return null;
    }
}
