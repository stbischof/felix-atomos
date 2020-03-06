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
package org.apache.felix.atomos.maven.reflect;

import java.util.Arrays;

public class ConstructorConfig
{
    public String[] methodParameterTypes;

    public ConstructorConfig()
    {
        this(new String[] {});
    }

    public ConstructorConfig(String[] methodParameters)
    {
        methodParameterTypes = methodParameters;
    }

    @Override
    public String toString()
    {
        return "ConstructorConfig [methodParameters=" + Arrays.toString(methodParameterTypes)
        + "]";
    }
}


