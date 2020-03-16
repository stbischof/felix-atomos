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
package org.apache.felix.atomos.substrate.core.plugins;

import java.util.Map;

import org.apache.felix.atomos.substrate.api.PluginConfigBase;
import org.apache.felix.atomos.substrate.api.RegisterServiceCall;
import org.apache.felix.atomos.substrate.api.SubstrateContext;
import org.apache.felix.atomos.substrate.api.plugin.ComponentDescription;
import org.apache.felix.atomos.substrate.api.plugin.ComponentMetaDataPlugin;
import org.apache.felix.atomos.substrate.api.plugin.RegisterServicepPlugin;

public class GogoPlugin implements ComponentMetaDataPlugin<PluginConfigBase>, RegisterServicepPlugin<PluginConfigBase>
{
    private static final String OSGI_COMMAND_FUNCTION = "osgi.command.function";

    @Override
    public void init(PluginConfigBase config)
    {

        System.out.println("a");
        //        System.out.println(config.doNotCall());

    }

    @Override
    public void doComponentMetaData(ComponentDescription c, SubstrateContext context,
        ClassLoader classLoader)
    {

        Class<?> clazz;
        try
        {
            clazz = classLoader.loadClass(c.implementationClass());

            if (c.properties().containsKey(OSGI_COMMAND_FUNCTION))
            {
                Object oFunctions = c.properties().get(OSGI_COMMAND_FUNCTION);
                String[] functions = null;
                if (oFunctions instanceof String[])
                {
                    functions = (String[]) oFunctions;
                }
                else
                {
                    functions = new String[] { oFunctions.toString() };
                }
                addMethodsFromGogoCommand(clazz, functions, context);
            }
        }
        catch (ClassNotFoundException e)
        {
            e.printStackTrace();
        }
    }

    private static void addMethodsFromGogoCommand(Class<?> clazz, String[] functions,
        SubstrateContext context)
    {
        Class<?> tmpClass = clazz;
        for (String function : functions)
        {
            context.addMethod(function, tmpClass);
        }
        tmpClass = tmpClass.getSuperclass();

        if (tmpClass != null && !tmpClass.equals(Object.class))
        {
            addMethodsFromGogoCommand(tmpClass, functions, context);
        }
    }

    @Override
    public void doRegisterServiceCall(RegisterServiceCall registerServiceCall,
        SubstrateContext context, ClassLoader classLoader)
    {

        Map<String, ?> config = registerServiceCall.config();
        if (config.containsKey(OSGI_COMMAND_FUNCTION))
        {
            String[] functions = (String[]) config.get(OSGI_COMMAND_FUNCTION);

            addMethodsFromGogoCommand(registerServiceCall.service().getClass(), functions,
                context);
        }

    }

}
