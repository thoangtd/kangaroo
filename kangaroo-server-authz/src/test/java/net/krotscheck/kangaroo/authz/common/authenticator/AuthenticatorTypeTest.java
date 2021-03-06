/*
 * Copyright (c) 2017 Michael Krotscheck
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
 *
 */

package net.krotscheck.kangaroo.authz.common.authenticator;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for the authenticator type.
 *
 * @author Michael Krotscheck
 */
public final class AuthenticatorTypeTest {

    /**
     * Assert that expected entities are private, not public.
     */
    @Test
    public void testIsPrivate() {
        for (AuthenticatorType type : AuthenticatorType.values()) {
            if (type.in(AuthenticatorType.Password,
                    AuthenticatorType.Test,
                    AuthenticatorType.Google,
                    AuthenticatorType.Facebook)) {
                assertTrue(type.isPrivate());
            } else {
                assertFalse(type.isPrivate());
            }
        }
    }

    /**
     * Assert that expected entities are private, not public.
     */
    @Test
    public void testIn() {
        assertTrue(AuthenticatorType.Password.in(
                AuthenticatorType.Password,
                AuthenticatorType.Password
        ));
        assertTrue(AuthenticatorType.Password.in(
                AuthenticatorType.Test,
                AuthenticatorType.Password
        ));
        assertTrue(AuthenticatorType.Password.in(
                AuthenticatorType.Password
        ));
        assertFalse(AuthenticatorType.Password.in(
                AuthenticatorType.Test,
                AuthenticatorType.Test
        ));
        assertFalse(AuthenticatorType.Password.in());
        assertFalse(AuthenticatorType.Password.in(null));
    }

    /**
     * Assert that valueOf conversions works.
     */
    @Test
    public void testValueOf() {
        assertEquals(
                AuthenticatorType.Password,
                AuthenticatorType.valueOf("Password")
        );
        assertEquals(
                AuthenticatorType.Test,
                AuthenticatorType.valueOf("Test")
        );
        assertEquals(
                AuthenticatorType.Facebook,
                AuthenticatorType.valueOf("Facebook")
        );
        assertEquals(
                AuthenticatorType.Google,
                AuthenticatorType.valueOf("Google")
        );
    }
}
