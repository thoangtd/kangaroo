/*
 * Copyright (c) 2016 Michael Krotscheck
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.krotscheck.features.database.entity;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.Test;

/**
 * Unit tests for the client type.
 *
 * @author Michael Krotscheck
 */
public final class ClientTypeTest {

    /**
     * Assert that these enum types serialize into expected values.
     *
     * @throws Exception Json Serialization Exception.
     */
    @Test
    public void testSerialization() throws Exception {
        ObjectMapper m = new ObjectMapper();

        String authOutput = m.writeValueAsString(ClientType.Confidential);
        Assert.assertEquals("\"Confidential\"", authOutput);

        String bearerOutput = m.writeValueAsString(ClientType.Public);
        Assert.assertEquals("\"Public\"", bearerOutput);
    }

    /**
     * Assert that these enum types serialize into expected values.
     *
     * @throws Exception Json Serialization Exception.
     */
    @Test
    public void testDeserialization() throws Exception {
        ObjectMapper m = new ObjectMapper();
        ClientType authOutput =
                m.readValue("\"Confidential\"", ClientType.class);
        Assert.assertSame(authOutput, ClientType.Confidential);
        ClientType bearerOutput =
                m.readValue("\"Public\"", ClientType.class);
        Assert.assertSame(bearerOutput, ClientType.Public);
    }
}
