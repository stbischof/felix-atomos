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

import static org.apache.felix.atomos.substrate.core.TestConstants.DEP_ATOMOS_TESTS_TESTBUNDLES_REFLECT_COMMAND;
import static org.apache.felix.atomos.substrate.core.TestConstants.DEP_ATOMOS_TESTS_TESTBUNDLES_REFLECT_DTO;
import static org.apache.felix.atomos.substrate.core.TestConstants.DEP_ATOMOS_TESTS_TESTBUNDLES_SERVICE_CONTRACT;
import static org.apache.felix.atomos.substrate.core.TestConstants.DEP_ATOMOS_TESTS_TESTBUNDLES_SERVICE_IMPL;
import static org.apache.felix.atomos.substrate.core.TestConstants.DEP_ATOMOS_TESTS_TESTBUNDLES_SERVICE_IMPL_ACTIVATOR;
import static org.apache.felix.atomos.substrate.core.TestConstants.DEP_ATOMOS_TESTS_TESTBUNDLES_SERVICE_USER;
import static org.apache.felix.atomos.substrate.core.TestConstants.DEP_ORG_OSGI_DTO;
import static org.apache.felix.atomos.substrate.core.TestConstants.DEP_ORG_OSGI_FRAMEWORK;
import static org.apache.felix.atomos.substrate.core.TestConstants.filterConstructor;
import static org.apache.felix.atomos.substrate.core.TestConstants.filterMethod;
import static org.apache.felix.atomos.substrate.core.TestConstants.filterReflectConfigByClassName;
import static org.apache.felix.atomos.substrate.core.TestConstants.getDependencys;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.apache.felix.atomos.substrate.api.FileType;
import org.apache.felix.atomos.substrate.api.Launcher;
import org.apache.felix.atomos.substrate.api.reflect.ReflectConfig;
import org.apache.felix.atomos.substrate.api.reflect.ReflectConstructorConfig;
import org.apache.felix.atomos.substrate.api.reflect.ReflectFieldConfig;
import org.apache.felix.atomos.substrate.api.reflect.ReflectMethodConfig;
import org.apache.felix.atomos.substrate.core.plugins.BaseBundleActivatorPlugin;
import org.apache.felix.atomos.substrate.core.plugins.BaseComponentDescriptionPlugin;
import org.apache.felix.atomos.substrate.core.plugins.DirectoryFileCollectorPlugin;
import org.apache.felix.atomos.substrate.core.plugins.DirectoryFileCollectorPluginConfig;
import org.apache.felix.atomos.substrate.core.plugins.GogoPlugin;
import org.apache.felix.atomos.substrate.core.plugins.InvocatingBundleActivatorPlugin;
import org.apache.felix.atomos.substrate.core.plugins.OsgiDTOPlugin;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class ReflectConfigTest extends TestBase
{

    ContextImpl launch(List<Path> paths) throws IOException
    {
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

        return contextImpl;
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

    @Test
    void testActivateMethod(@TempDir Path tempDir) throws Exception
    {
        List<Path> paths = getDependencys(DEP_ATOMOS_TESTS_TESTBUNDLES_SERVICE_CONTRACT,
            DEP_ATOMOS_TESTS_TESTBUNDLES_SERVICE_IMPL);

        ContextImpl contextImpl = launch(paths);
        Collection<ReflectConfig> rcs = contextImpl.getReflectConfigs();

        ReflectConfig rc = filterReflectConfigByClassName(rcs,
            "org.apache.felix.atomos.tests.testbundles.service.impl.EchoImpl");
        assertThat(rc.getFields()).isEmpty();

        Optional<ReflectConstructorConfig> oc = filterConstructor(rc, new String[] {});
        assertTrue(oc.isPresent());

        Optional<ReflectMethodConfig> omc1 = filterMethod(rc, "activate", null);
        assertTrue(omc1.isPresent());
    }

    @Test
    void testReflectBundleActivator(@TempDir Path tempDir) throws Exception
    {
        List<Path> paths = getDependencys(DEP_ORG_OSGI_FRAMEWORK,
            DEP_ATOMOS_TESTS_TESTBUNDLES_SERVICE_IMPL_ACTIVATOR);

        ContextImpl contextImpl = launch(paths);
        Collection<ReflectConfig> rcs = contextImpl.getReflectConfigs();

        ReflectConfig rc = filterReflectConfigByClassName(rcs,
            "org.apache.felix.atomos.tests.testbundles.service.impl.activator.Activator");
        assertThat(rc.getFields()).isEmpty();

        Optional<ReflectConstructorConfig> oc = filterConstructor(rc, new String[] {});
        assertTrue(oc.isPresent());

        Optional<ReflectMethodConfig> omc1 = filterMethod(rc, "start",
            new String[] { "org.osgi.framework.BundleContext" });
        assertTrue(omc1.isPresent());
        Optional<ReflectMethodConfig> omc2 = filterMethod(rc, "stop",
            new String[] { "org.osgi.framework.BundleContext" });
        assertTrue(omc2.isPresent());
    }

    @Test
    void testBundleActivatorMagic(@TempDir Path tempDir) throws Exception
    {
        List<Path> paths = getDependencys(DEP_ORG_OSGI_FRAMEWORK,
            DEP_ATOMOS_TESTS_TESTBUNDLES_SERVICE_IMPL_ACTIVATOR,
            DEP_ATOMOS_TESTS_TESTBUNDLES_SERVICE_CONTRACT);

        ContextImpl contextImpl = launch(paths);
        Collection<ReflectConfig> rcs = contextImpl.getReflectConfigs();

        ReflectConfig rc = filterReflectConfigByClassName(rcs,
            "org.apache.felix.atomos.tests.testbundles.service.impl.activator.ActivatorEcho");
        assertThat(rc.getConstructors()).isEmpty();
        assertThat(rc.getFields()).isEmpty();

        Optional<ReflectMethodConfig> omc1 = filterMethod(rc, "echo", null);
        assertTrue(omc1.isPresent());
    }

    @Test
    void testDTO(@TempDir Path tempDir) throws Exception
    {

        List<Path> paths = getDependencys(DEP_ATOMOS_TESTS_TESTBUNDLES_REFLECT_DTO,
            DEP_ORG_OSGI_DTO);

        ContextImpl contextImpl = launch(paths);
        Collection<ReflectConfig> rcs = contextImpl.getReflectConfigs();

        ReflectConfig rc = filterReflectConfigByClassName(rcs,
            "org.apache.felix.atomos.tests.testbundles.reflect.command.OneDTO");
        assertThat(rc.getConstructors()).isEmpty();
        assertThat(rc.getMethods()).isEmpty();

        assertNotNull(rc.getFields());
        assertEquals(1, rc.getFields().size());
        assertEquals("one", rc.getFields().stream().findAny().map(
            ReflectFieldConfig::getFieldName).get());

    }

    @Test
    void testGOGOCommand(@TempDir Path tempDir) throws Exception
    {
        List<Path> paths = getDependencys(DEP_ATOMOS_TESTS_TESTBUNDLES_REFLECT_COMMAND);
        ContextImpl contextImpl = launch(paths);
        Collection<ReflectConfig> rcs = contextImpl.getReflectConfigs();

        ReflectConfig rc1 = filterReflectConfigByClassName(rcs,
            "org.apache.felix.atomos.tests.testbundles.reflect.command.AbstractCmd");
        assertThat(rc1.getConstructors()).isEmpty();
        assertThat(rc1.getFields()).isEmpty();

        Optional<ReflectMethodConfig> rc1mc1 = filterMethod(rc1, "multiple", null);
        assertNotNull(rc1mc1);

        ReflectConfig rc2 = filterReflectConfigByClassName(rcs,
            "org.apache.felix.atomos.tests.testbundles.reflect.command.CmdExample");

        assertThat(rc2.getFields()).isEmpty();

        Optional<ReflectConstructorConfig> rc2cc1 = filterConstructor(rc2,
            new String[] {});
        assertTrue(rc2cc1.isPresent());

        Optional<ReflectMethodConfig> rc2mc1 = filterMethod(rc2, "a", null);
        assertTrue(rc2mc1.isPresent());

        Optional<ReflectMethodConfig> rc2mc2 = filterMethod(rc2, "multiple", null);
        assertTrue(rc2mc2.isPresent());

        Optional<ReflectMethodConfig> rc2mc3 = filterMethod(rc2, "single", null);
        assertTrue(rc2mc3.isPresent());

    }

    @Test
    void testReference(@TempDir Path tempDir) throws Exception
    {

        try
        {

            List<Path> paths = getDependencys(
                DEP_ATOMOS_TESTS_TESTBUNDLES_SERVICE_CONTRACT,
                DEP_ATOMOS_TESTS_TESTBUNDLES_SERVICE_USER);

            ContextImpl contextImpl = launch(paths);
            Collection<ReflectConfig> rcs = contextImpl.getReflectConfigs();

            ReflectConfig rc1 = filterReflectConfigByClassName(rcs,
                "org.apache.felix.atomos.tests.testbundles.service.contract.Echo");
            assertThat(rc1.getConstructors()).isEmpty();
            assertThat(rc1.getFields()).isEmpty();
            assertThat(rc1.getMethods()).isEmpty();

            ReflectConfig rc2 = filterReflectConfigByClassName(rcs,
                "org.apache.felix.atomos.tests.testbundles.service.user.EchoUser");

            assertThat(rc2.getFields()).isEmpty();

            Optional<ReflectConstructorConfig> rc2cc1 = filterConstructor(rc2,
                new String[] {});
            assertTrue(rc2cc1.isPresent());

            Optional<ReflectMethodConfig> rc2mc1 = filterMethod(rc2, "activate", null);
            assertTrue(rc2mc1.isPresent());

            Optional<ReflectMethodConfig> rc2mc2 = filterMethod(rc2, "setEcho", null);
            assertTrue(rc2mc2.isPresent());

            Optional<ReflectMethodConfig> rc2mc3 = filterMethod(rc2, "unsetEcho", null);
            assertTrue(rc2mc3.isPresent());

            ReflectConfig rc3 = filterReflectConfigByClassName(rcs,
                "org.apache.felix.atomos.tests.testbundles.service.user.EchoUser2");
            assertThat(rc3.getFields()).isEmpty();
            assertThat(rc3.getMethods()).isEmpty();
            Optional<ReflectConstructorConfig> rc3cc1 = filterConstructor(rc3,
                new String[] { "java.util.Map",
            "org.apache.felix.atomos.tests.testbundles.service.contract.Echo" });
            assertTrue(rc3cc1.isPresent());
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}