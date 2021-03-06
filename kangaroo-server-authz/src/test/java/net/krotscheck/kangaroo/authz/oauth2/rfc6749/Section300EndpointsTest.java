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

package net.krotscheck.kangaroo.authz.oauth2.rfc6749;

import org.junit.Test;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import static org.junit.Assert.assertNotEquals;

/**
 * These tests assert that the expected endpoints defined in Section 3 exist.
 *
 * @see <a href="https://tools.ietf.org/html/rfc6749#section-3">https://tools.ietf.org/html/rfc6749#section-3</a>
 */
public final class Section300EndpointsTest extends AbstractRFC6749Test {

    /**
     * Assert that the /authorize endpoint exists.
     */
    @Test
    public void testAuthorizationEndpoint() {
        Response response = target("/authorize").request().get();
        assertNotEquals(Status.NOT_FOUND.getStatusCode(),
                response.getStatus());
    }

    /**
     * Assert that the /token endpoint exists.
     */
    @Test
    public void testTokenEndpoint() {
        Response response = target("/token").request().get();
        assertNotEquals(Status.NOT_FOUND.getStatusCode(),
                response.getStatus());
    }
}
