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

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.HashMap;

import org.apache.felix.atomos.substrate.api.Launcher;
import org.apache.felix.atomos.substrate.core.plugins.DirectoryFileCollectorPlugin;
import org.junit.jupiter.api.Test;

public class LauncherTest
{

    @Test
    void testName() throws Exception
    {
        assertNotNull(Launcher.defaultBuilder().build());
    }

    @Test
    void testPluginFileCollector() throws Exception
    {
        assertNotNull(Launcher.emptyBuilder().addPlugin(
            DirectoryFileCollectorPlugin.class, new HashMap<String, Object>()).build());
    }

}
