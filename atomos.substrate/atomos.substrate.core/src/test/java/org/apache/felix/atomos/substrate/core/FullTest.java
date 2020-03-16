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

import static org.apache.felix.atomos.substrate.core.TestConstants.DEP_ATOMOS_TESTS_TESTBUNDLES_SERVICE_CONTRACT;
import static org.apache.felix.atomos.substrate.core.TestConstants.DEP_ATOMOS_TESTS_TESTBUNDLES_SERVICE_IMPL;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;

import org.apache.felix.atomos.substrate.api.FileType;
import org.apache.felix.atomos.substrate.api.Launcher;
import org.apache.felix.atomos.substrate.api.reflect.ReflectConfig;
import org.apache.felix.atomos.substrate.core.plugins.BaseBundleActivatorPlugin;
import org.apache.felix.atomos.substrate.core.plugins.BaseComponentDescriptionPlugin;
import org.apache.felix.atomos.substrate.core.plugins.DirectoryFileCollectorPlugin;
import org.apache.felix.atomos.substrate.core.plugins.DirectoryFileCollectorPluginConfig;
import org.apache.felix.atomos.substrate.core.plugins.GogoPlugin;
import org.apache.felix.atomos.substrate.core.plugins.InvocatingBundleActivatorPlugin;
import org.apache.felix.atomos.substrate.core.plugins.OsgiDTOPlugin;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class FullTest extends TestBase
{

    @Test
    void testDirectoryFileCollectorPlugin(@TempDir Path tempDir) throws Exception
    {

        List<Path> paths = TestConstants.getDependencys(
            DEP_ATOMOS_TESTS_TESTBUNDLES_SERVICE_CONTRACT,
            DEP_ATOMOS_TESTS_TESTBUNDLES_SERVICE_IMPL);

        DirectoryFileCollectorPluginConfig cfg = fileConfig(FileType.ARTEFACT, paths);

        Launcher launcher = Launcher.emptyBuilder()//
            .addPlugin(DirectoryFileCollectorPlugin.class, cfg)//
            .addPlugin(OsgiDTOPlugin.class, cfg)//
            .addPlugin(GogoPlugin.class, cfg)//
            .addPlugin(BaseBundleActivatorPlugin.class, cfg)//
            .addPlugin(InvocatingBundleActivatorPlugin.class, cfg)//
            .addPlugin(BaseComponentDescriptionPlugin.class, cfg)//
            .build();

        ContextImpl contextImpl = new ContextImpl();
        launcher.execute(contextImpl);

        Collection<ReflectConfig> rcs = contextImpl.getReflectConfigs();
        //        assertThat(testP).containsExactlyInAnyOrder(paths.toArray(Path[]::new));
        System.out.println(contextImpl);
    }

    private DirectoryFileCollectorPluginConfig fileConfig(FileType fileType, List<Path> files)
        throws IOException
    {

        DirectoryFileCollectorPluginConfig cfg = new DirectoryFileCollectorPluginConfig()
        {

            @Override
            public List<Path> paths()
            {

                return files;
            }

            @Override
            public List<String> filters()
            {
                return null;
            }

            @Override
            public FileType fileType()
            {
                return fileType;
            }
        };
        return cfg;
    }
}