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

package net.krotscheck.kangaroo.authz.admin.v1.resource;

import net.krotscheck.kangaroo.authz.admin.Scope;
import net.krotscheck.kangaroo.authz.common.database.entity.AbstractAuthzEntity;
import net.krotscheck.kangaroo.authz.common.database.entity.Application;
import net.krotscheck.kangaroo.authz.common.database.entity.Client;
import net.krotscheck.kangaroo.authz.common.database.entity.ClientRedirect;
import net.krotscheck.kangaroo.authz.common.database.entity.ClientType;
import net.krotscheck.kangaroo.authz.test.ApplicationBuilder.ApplicationContext;
import net.krotscheck.kangaroo.common.hibernate.id.IdUtil;
import net.krotscheck.kangaroo.common.response.ListResponseEntity;
import org.apache.commons.lang3.RandomStringUtils;
import org.hibernate.Session;
import org.junit.Test;
import org.junit.runners.Parameterized;

import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriBuilder;
import java.math.BigInteger;
import java.net.URI;
import java.util.Arrays;
import java.util.Collection;

import static org.junit.Assert.assertEquals;


/**
 * Unit test suite for the client redirect subresource.
 */
public final class ClientRedirectServiceCRUDTest
        extends AbstractSubserviceCRUDTest<Client, ClientRedirect> {

    /**
     * Convenience generic type for response decoding.
     */
    private static final GenericType<ListResponseEntity<ClientRedirect>>
            LIST_TYPE =
            new GenericType<ListResponseEntity<ClientRedirect>>() {

            };

    /**
     * Create a new instance of this parameterized test.
     *
     * @param clientType    The type of  client.
     * @param tokenScope    The client scope to issue.
     * @param createUser    Whether to create a new user.
     * @param shouldSucceed Should this test succeed?
     */
    public ClientRedirectServiceCRUDTest(final ClientType clientType,
                                         final String tokenScope,
                                         final Boolean createUser,
                                         final Boolean shouldSucceed) {
        super(Client.class, ClientRedirect.class, clientType, tokenScope,
                createUser, shouldSucceed);
    }

    /**
     * Test parameters.
     *
     * @return List of parameters used to reconstruct this test.
     */
    @Parameterized.Parameters
    public static Collection parameters() {
        return Arrays.asList(
                new Object[]{
                        ClientType.Implicit,
                        Scope.CLIENT_ADMIN,
                        false,
                        true
                },
                new Object[]{
                        ClientType.Implicit,
                        Scope.CLIENT,
                        false,
                        true
                },
                new Object[]{
                        ClientType.Implicit,
                        Scope.CLIENT_ADMIN,
                        true,
                        true
                },
                new Object[]{
                        ClientType.Implicit,
                        Scope.CLIENT,
                        true,
                        false
                },
                new Object[]{
                        ClientType.ClientCredentials,
                        Scope.CLIENT_ADMIN,
                        false,
                        true
                },
                new Object[]{
                        ClientType.ClientCredentials,
                        Scope.CLIENT,
                        false,
                        false
                });
    }

    /**
     * Return the appropriate list type for this test suite.
     *
     * @return The list type, used for test decoding.
     */
    @Override
    protected GenericType<ListResponseEntity<ClientRedirect>> getListType() {
        return LIST_TYPE;
    }

    /**
     * Return the token scope required for admin access on this test.
     *
     * @return The correct scope string.
     */
    @Override
    protected String getAdminScope() {
        return Scope.CLIENT_ADMIN;
    }

    /**
     * Return the token scope required for generic user access.
     *
     * @return The correct scope string.
     */
    @Override
    protected String getRegularScope() {
        return Scope.CLIENT;
    }

    /**
     * Construct the request URL for this test given a specific resource ID.
     *
     * @param id The ID to use.
     * @return The resource URL.
     */
    @Override
    protected URI getUrlForId(final String id) {
        String parentId = "";

        Session s = getSession();
        s.getTransaction().begin();
        try {
            ClientRedirect r = s.get(ClientRedirect.class,
                    IdUtil.fromString(id));
            parentId = IdUtil.toString(r.getClient().getId());
        } catch (Exception e) {
            parentId = IdUtil.toString(getParentEntity(getAdminContext())
                    .getId());
        } finally {
            s.getTransaction().commit();
        }

        return getUrlForEntity(parentId, id);
    }

    /**
     * Construct the request URL for this test given a specific resource ID.
     *
     * @param entity The entity to use.
     * @return The resource URL.
     */
    @Override
    protected URI getUrlForEntity(final AbstractAuthzEntity entity) {
        String parentId = "";
        String childId = "";

        ClientRedirect redirect = (ClientRedirect) entity;
        if (redirect == null) {
            return getUrlForId(null);
        } else {
            BigInteger redirectId = redirect.getId();
            childId = redirectId == null ? null : IdUtil.toString(redirectId);
        }

        Client client = redirect.getClient();
        if (client == null) {
            return getUrlForId(null);
        } else {
            BigInteger clientId = client.getId();
            parentId = clientId == null ? null : IdUtil.toString(clientId);
        }
        return getUrlForEntity(parentId, childId);
    }

    /**
     * Construct the request URL for this test given a specific resource ID.
     *
     * @param parentId The parent ID.
     * @param childId  The Child ID.
     * @return The resource URL.
     */
    private URI getUrlForEntity(final String parentId, final String childId) {
        UriBuilder builder = UriBuilder
                .fromPath("/client")
                .path(parentId)
                .path("redirect");

        if (childId != null) {
            builder.path(childId);
        }

        return builder.build();
    }

    /**
     * Return the correct parent entity type from the provided context.
     *
     * @param context The context to extract the value from.
     * @return The requested entity type under test.
     */
    @Override
    protected Client getParentEntity(final ApplicationContext context) {
        return getAttached(context.getClient());
    }

    /**
     * Given a parent entity and a context, create a valid entity.
     *
     * @param context The environment context.
     * @param parent  The parent entity.
     * @return A valid entity.
     */
    @Override
    protected ClientRedirect createValidEntity(final ApplicationContext context,
                                               final Client parent) {
        ClientRedirect r = new ClientRedirect();
        r.setClient(parent);
        r.setUri(URI.create(String.format("http://%s.example.com",
                IdUtil.next())));
        return r;
    }

    /**
     * Create a valid parent entity for the given context.
     *
     * @param context The environment context.
     * @return A valid entity.
     */
    @Override
    protected Client createParentEntity(final ApplicationContext context) {
        Application a = getAttached(context.getApplication());
        Client c = new Client();
        c.setApplication(a);
        c.setName(IdUtil.toString(IdUtil.next()));
        c.setType(ClientType.AuthorizationGrant);
        return c;
    }

    /**
     * Return the correct testingEntity type from the provided context.
     *
     * @param context The context to extract the value from.
     * @return The requested entity type under test.
     */
    @Override
    protected ClientRedirect getEntity(final ApplicationContext context) {
        return getAttached(context.getRedirect());
    }

    /**
     * Return a new, empty entity.
     *
     * @return The requested entity type under test.
     */
    @Override
    protected ClientRedirect getNewEntity() {
        ClientRedirect newEntity = new ClientRedirect();
        newEntity.setClient(getAttached(getAdminContext().getClient()));
        return newEntity;
    }

    /**
     * Assert that some users may create entities for other parents.
     *
     * @throws Exception Exception encountered during test.
     */
    @Test
    public void testPostNoUri() throws Exception {
        ClientRedirect testEntity = createValidEntity(getSecondaryContext());
        testEntity.setUri(null);

        // Issue the request.
        Response r = postEntity(testEntity, getAdminToken());
        assertErrorResponse(r, Status.BAD_REQUEST);
    }

    /**
     * Assert that attempting to create a duplicate record fails.
     *
     * @throws Exception Exception encountered during test.
     */
    @Test
    public void testPostDuplicate() throws Exception {
        ApplicationContext testContext = getAdminContext();
        Client c = getAttached(getParentEntity(testContext));
        URI existing = c.getRedirects().get(0).getUri();

        ClientRedirect testEntity = new ClientRedirect();
        testEntity.setClient(c);
        testEntity.setUri(existing);

        // Issue the request.
        Response r = postEntity(testEntity, getAdminToken());
        if (shouldSucceed()) {
            assertErrorResponse(r, Status.CONFLICT);
        } else {
            assertErrorResponse(r, Status.BAD_REQUEST);
        }
    }

    /**
     * Assert that we can update entities.
     *
     * @throws Exception Exception encountered during test.
     */
    @Test
    public void testPut() throws Exception {
        ApplicationContext builder = getAdminContext();
        ClientRedirect oldEntity = builder.getRedirect();

        // Copy the most recent, then use the redirect from the previous.
        ClientRedirect cr = new ClientRedirect();
        cr.setClient(oldEntity.getClient());
        cr.setId(oldEntity.getId());
        cr.setUri(URI.create("http://new.example.com/redirect"));

        // Issue the request.
        Response r = putEntity(cr, getAdminToken());
        if (shouldSucceed()) {
            ClientRedirect response = r.readEntity(ClientRedirect.class);
            assertEquals(Status.OK.getStatusCode(), r.getStatus());
            assertEquals(cr, response);
        } else {
            assertErrorResponse(r, Status.NOT_FOUND);
        }
    }

    /**
     * Assert that we cannot create an entity without a URI.
     *
     * @throws Exception Exception encountered during test.
     */
    @Test
    public void testPutNoUri() throws Exception {
        ApplicationContext builder = getAdminContext();
        ClientRedirect oldEntity = builder.getRedirect();

        // Copy the most recent, then use the redirect from the previous.
        ClientRedirect duplicateRedirect = new ClientRedirect();
        duplicateRedirect.setClient(oldEntity.getClient());
        duplicateRedirect.setId(oldEntity.getId());
        duplicateRedirect.setUri(null);

        // Issue the request.
        Response r = putEntity(duplicateRedirect, getAdminToken());
        if (shouldSucceed()) {
            assertErrorResponse(r, Status.BAD_REQUEST);
        } else {
            assertErrorResponse(r, Status.NOT_FOUND);
        }
    }

    /**
     * Assert that you cannot edit an entity to become a duplicate.
     *
     * @throws Exception Exception encountered during test.
     */
    @Test
    public void testPutDuplicate() throws Exception {
        String rawUrl = String.format("http://%s/redirect",
                RandomStringUtils.randomAlphabetic(10));
        ApplicationContext builder = getAdminContext().getBuilder()
                .redirect(rawUrl)
                .redirect()
                .build();
        ClientRedirect oldEntity = builder.getRedirect();

        // Copy the most recent, then use the redirect from the previous.
        ClientRedirect duplicateRedirect = new ClientRedirect();
        duplicateRedirect.setClient(oldEntity.getClient());
        duplicateRedirect.setId(oldEntity.getId());
        duplicateRedirect.setUri(URI.create(rawUrl));

        // Issue the request.
        Response r = putEntity(duplicateRedirect, getAdminToken());
        if (shouldSucceed()) {
            assertErrorResponse(r, Status.CONFLICT);
        } else {
            assertErrorResponse(r, Status.NOT_FOUND);
        }
    }

    /**
     * Assert that the admin app cannot be deleted, even if we have all the
     * credentials in the world.
     *
     * @throws Exception Exception encountered during test.
     */
    @Test
    public void testDeleteAdminApp() throws Exception {
        ApplicationContext context = getAdminContext();

        // Issue the request.
        Response r = deleteEntity(context.getRedirect(), getAdminToken());

        if (shouldSucceed()) {
            assertErrorResponse(r, Status.FORBIDDEN);
        } else {
            assertErrorResponse(r, Status.NOT_FOUND);
        }
    }

    /**
     * Sanity test for coverage on the scope getters.
     *
     * @throws Exception Exception encountered during test.
     */
    @Test
    public void testScopes() throws Exception {
        ClientRedirectService cs = new ClientRedirectService(IdUtil.next());

        assertEquals(Scope.CLIENT_ADMIN, cs.getAdminScope());
        assertEquals(Scope.CLIENT, cs.getAccessScope());
    }
}
