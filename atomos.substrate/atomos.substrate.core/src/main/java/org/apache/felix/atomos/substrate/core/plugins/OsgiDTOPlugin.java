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

import java.lang.reflect.Field;

import org.apache.felix.atomos.substrate.api.PluginConfigBase;
import org.apache.felix.atomos.substrate.api.SubstrateContext;
import org.apache.felix.atomos.substrate.api.plugin.ClassPlugin;

public class OsgiDTOPlugin implements ClassPlugin<PluginConfigBase>
{




    @Override
    public void init(PluginConfigBase config)
    {

    }

    @Override
    public void doClass(Class<?> clazz, SubstrateContext context)
    {
        boolean isDTO = false;
        Class<?> c = clazz;
        while (c != null && c != Object.class)
        {
            if ("org.osgi.dto.DTO".equals(c.getName()))
            {
                isDTO = true;
                break;
            }
            c = c.getSuperclass();
        }

        if (isDTO)
        {
            for (Field field : clazz.getFields())
            {
                context.addField(field.getName(), clazz);
            }
        }
    }

}
