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
package org.apache.felix.atomos.substrate.core.reflect;

import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.felix.atomos.substrate.api.reflect.ReflectConfig;
import org.apache.felix.atomos.substrate.api.reflect.ReflectConstructorConfig;
import org.apache.felix.atomos.substrate.api.reflect.ReflectFieldConfig;
import org.apache.felix.atomos.substrate.api.reflect.ReflectMethodConfig;

public class ReflectConfigImpl implements ReflectConfig
{
    public static Comparator<ReflectMethodConfig> mc = new Comparator<>()
    {
        @Override
        public int compare(ReflectMethodConfig o1, ReflectMethodConfig o2)
        {
            int i = o1.getName().compareTo(o2.getName());
            if (i == 0)
            {
                return cc.compare(o1, o2);
            }
            return i;
        }
    };

    public static Comparator<ReflectFieldConfig> fc = new Comparator<>()
    {
        @Override
        public int compare(ReflectFieldConfig o1, ReflectFieldConfig o2)
        {
            return o1.getFieldName().compareTo(o2.getFieldName());

        }
    };

    public static Comparator<ReflectConstructorConfig> cc = new Comparator<>()
    {

        @Override
        public int compare(ReflectConstructorConfig o1, ReflectConstructorConfig o2)
        {
            String s1 = o1.getMethodParameterTypes() == null ? ""
                : Stream.of(o1.getMethodParameterTypes()).collect(
                    Collectors.joining(","));
            String s2 = o2.getMethodParameterTypes() == null ? ""
                : Stream.of(o2.getMethodParameterTypes()).collect(
                    Collectors.joining(","));

            return s1.compareTo(s2);
        }
    };
    String className;

    private boolean allPublicConstructors = false;
    private boolean allPublicMethods = false;
    private boolean allPublicFields = false;

    private final Set<ConstructorConfigImpl> constructor = new TreeSet<>(cc);
    private final Set<FieldConfigImpl> fields = new TreeSet<>(fc);
    private final Set<MethodConfigImpl> methods = new TreeSet<>(mc);

    private ReflectConfigImpl()
    {
    }

    public ReflectConfigImpl(String className)
    {
        this();
        this.className = className;
    }

    @Override
    public boolean equals(Object other)
    {
        if (!(other instanceof ReflectConfigImpl))
        {
            return false;
        }
        return className == ((ReflectConfigImpl) other).className;
    }

    @Override
    public String getClassName()
    {
        return className;
    }

    @Override
    public boolean isAllPublicConstructors()
    {
        return allPublicConstructors;
    }

    @Override
    public boolean isAllPublicMethods()
    {
        return allPublicMethods;
    }

    @Override
    public boolean isAllPublicFields()
    {
        return allPublicFields;
    }

    @Override
    public Set<ReflectConstructorConfig> getConstructors()
    {
        return Set.copyOf(constructor);
    }

    @Override
    public Set<ReflectFieldConfig> getFields()
    {
        return Set.copyOf(fields);
    }

    @Override
    public Set<ReflectMethodConfig> getMethods()
    {

        return Set.copyOf(methods);
    }

    public void add(ConstructorConfigImpl constructorConfigImpl)
    {
        constructor.add(constructorConfigImpl);
    }

    public void add(MethodConfigImpl methodConfigImpl)
    {
        methods.add(methodConfigImpl);
    }

    public void add(FieldConfigImpl fieldConfigImpl)
    {
        fields.add(fieldConfigImpl);
    }

    public void setAllPublicConstructors(boolean b)
    {

        allPublicConstructors = b;
    }

    public void setAllPublicFields(boolean b)
    {
        allPublicFields = b;
    }

    /**
     * @param b
     */
    public void setAllPublicMethods(boolean b)
    {
        allPublicMethods = b;
    }

}
