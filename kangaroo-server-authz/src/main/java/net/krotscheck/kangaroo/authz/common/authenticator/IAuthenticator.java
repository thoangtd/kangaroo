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

import net.krotscheck.kangaroo.authz.common.authenticator.exception.MisconfiguredAuthenticatorException;
import net.krotscheck.kangaroo.authz.common.database.entity.Authenticator;
import net.krotscheck.kangaroo.authz.common.database.entity.UserIdentity;
import net.krotscheck.kangaroo.common.exception.KangarooException;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.Map;

/**
 * This interface describes the methods used during user authentication,
 * responsible for interfacing with a third party authentication provider.
 * All authentication MUST be performed via redirect.
 *
 * @author Michael Krotscheck
 */
public interface IAuthenticator {

    /**
     * Delegate an authentication request to a third party authentication
     * provider, such as Google, Facebook, etc.
     *
     * @param configuration The authenticator configuration.
     * @param callback      The redirect, on this server, where the response
     *                      should go.
     * @return An HTTP response, redirecting the client to the next step.
     */
    Response delegate(Authenticator configuration,
                      URI callback);

    /**
     * Validate that a particular authentication configuration is valid for
     * this IdP.
     *
     * @param authenticator The authenticator configuration.
     * @throws KangarooException Thrown if the internal parameters
     *                           are invalid.
     */
    default void validate(Authenticator authenticator)
            throws KangarooException {

        // If there's no authenticator...
        if (authenticator == null) {
            return;
        }

        // If we have any configuration values, throw an exception.
        Map<String, String> config = authenticator.getConfiguration();
        if (config == null) {
            return;
        }
        if (config.size() > 0) {
            throw new MisconfiguredAuthenticatorException();
        }
    }

    /**
     * Authenticate and/or create a user identity for a specific client, given
     * the URI from an authentication delegate.
     *
     * @param authenticator The authenticator configuration.
     * @param parameters    Parameters for the authenticator, retrieved from
     *                      an appropriate source.
     * @param callback      The redirect that was provided to the original
     *                      authorize call.
     * @return A user identity, or a runtime error that will be sent back.
     */
    UserIdentity authenticate(Authenticator authenticator,
                              MultivaluedMap<String, String> parameters,
                              URI callback);
}
