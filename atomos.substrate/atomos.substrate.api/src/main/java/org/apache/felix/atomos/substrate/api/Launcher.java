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

import java.util.ServiceLoader;

public interface Launcher
{
    String SYS_PROP_SEPARATOR = ";";
    String SYS_PROP_PLUGINS = "org.apache.felix.atomos.substrate.plugins";

    static LauncherBuilder defaultBuilder()
    {
        return builder(false);
    }

    static LauncherBuilder emptyBuilder()
    {
        return builder(true);
    }

    static LauncherBuilder builder(boolean empty)
    {

        LauncherBuilder launcherBuilder = ServiceLoader.load(
            Launcher.class.getModule().getLayer(),
            LauncherBuilder.class).findFirst().orElseThrow(
                () -> new RuntimeException(
                    String.format("ServiceLoader could not find found: %s",
                        LauncherBuilder.class.getName())));

        launcherBuilder.init(empty);
        return launcherBuilder;


    }
}
