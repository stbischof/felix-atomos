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

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.apache.felix.atomos.substrate.core.reflect.ConstructorConfigImpl;
import org.apache.felix.atomos.substrate.core.reflect.FieldConfigImpl;
import org.apache.felix.atomos.substrate.core.reflect.MethodConfigImpl;
import org.apache.felix.atomos.substrate.core.reflect.ReflectConfigImpl;
import org.junit.jupiter.api.Test;

public class ReflectConfigJsonTest extends TestBase
{
    @Test
    void testJsonSimple() throws Exception
    {
        ReflectConfigImpl rc = new ReflectConfigImpl("a");
        String json = ReflectConfigUtil.json(rc);
        json = shrinkJson(json);
        assertEquals("{\"name\":\"a\"}", json);
    }

    @Test
    void testJson() throws Exception
    {
        ReflectConfigImpl rc = new ReflectConfigImpl("a");
        rc.add(new FieldConfigImpl("f1"));
        rc.add(new FieldConfigImpl("f2"));

        rc.add(new ConstructorConfigImpl());
        rc.add(new ConstructorConfigImpl(new String[] { "p1" }));

        rc.add(new MethodConfigImpl("m1", null));
        rc.add(new MethodConfigImpl("m2", new String[] { "m2p1" }));
        rc.add(new MethodConfigImpl("m3", new String[] { "m3p1", "m3p2" }));

        String json = ReflectConfigUtil.json(rc);
        json = shrinkJson(json);
        String exp = "{\"name\":\"a\",\"fields\":[{\"name\":\"f1\"},{\"name\":\"f2\"}],\"methods\":[{\"name\":\"<init>\",\"parameterTypes\":[]},{\"name\":\"<init>\",\"parameterTypes\":[\"p1\"]},{\"name\":\"m1\"},{\"name\":\"m2\",\"parameterTypes\":[\"m2p1\"]},{\"name\":\"m3\",\"parameterTypes\":[\"m3p1\",\"m3p2\"]}]}";
        assertEquals(exp, json);
    }


    private String shrinkJson(String json)
    {
        json = json.replace(" ", "").replace("\n", "");
        return json;
    }

    @Test
    void testJsonFull() throws Exception
    {
        List<ReflectConfigImpl> rcs = new ArrayList<>();
        rcs.add(new ReflectConfigImpl("z"));
        rcs.add(new ReflectConfigImpl("a"));
        String json = ReflectConfigUtil.json(rcs);
        json = shrinkJson(json);
        assertEquals("[{\"name\":\"a\"},{\"name\":\"z\"}]", json);
    }


}