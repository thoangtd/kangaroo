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

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;
import io.swagger.annotations.AuthorizationScope;
import net.krotscheck.kangaroo.authz.admin.Scope;
import net.krotscheck.kangaroo.authz.admin.v1.auth.ScopesAllowed;
import net.krotscheck.kangaroo.authz.common.database.entity.Application;
import net.krotscheck.kangaroo.authz.common.database.entity.Role;
import net.krotscheck.kangaroo.authz.common.database.entity.User;
import net.krotscheck.kangaroo.authz.common.database.util.SortUtil;
import net.krotscheck.kangaroo.common.hibernate.id.IdUtil;
import net.krotscheck.kangaroo.common.hibernate.transaction.Transactional;
import net.krotscheck.kangaroo.common.response.ApiParam;
import net.krotscheck.kangaroo.common.response.ListResponseBuilder;
import net.krotscheck.kangaroo.common.response.SortOrder;
import org.apache.lucene.search.Query;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.search.FullTextQuery;
import org.hibernate.search.query.dsl.BooleanJunction;
import org.hibernate.search.query.dsl.QueryBuilder;
import org.jvnet.hk2.annotations.Optional;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.math.BigInteger;
import java.net.URI;
import java.util.Objects;


/**
 * A RESTful API that permits the management of application resources.
 *
 * @author Michael Krotscheck
 */
@Path("/application")
@ScopesAllowed({Scope.APPLICATION, Scope.APPLICATION_ADMIN})
@Transactional
@Api(tags = "Application",
        authorizations = {
                @Authorization(value = "Kangaroo", scopes = {
                        @AuthorizationScope(
                                scope = Scope.APPLICATION,
                                description = "Modify one application."),
                        @AuthorizationScope(
                                scope = Scope.APPLICATION_ADMIN,
                                description = "Modify all applications.")
                })
        })
public final class ApplicationService extends AbstractService {

    /**
     * Search the applications in the system.
     *
     * @param offset      The offset of the first applications to fetch.
     * @param limit       The number of data sets to fetch.
     * @param queryString The search term for the query.
     * @param ownerId     An optional user ID to filter by.
     * @return A list of search results.
     */
    @GET
    @Path("/search")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Search applications")
    @SuppressWarnings({"CPD-START"})
    public Response search(
            @DefaultValue("0") @QueryParam("offset") final Integer offset,
            @DefaultValue("10") @QueryParam("limit") final Integer limit,
            @DefaultValue("") @QueryParam("q") final String queryString,
            @io.swagger.annotations.ApiParam(type = "string")
            @Optional @QueryParam("owner") final BigInteger ownerId) {

        // Start a query builder...
        QueryBuilder builder = getSearchFactory()
                .buildQueryBuilder()
                .forEntity(Application.class)
                .get();
        BooleanJunction junction = builder.bool();

        Query fuzzy = builder.keyword()
                .fuzzy()
                .onFields(new String[]{"name"})
                .matching(queryString)
                .createQuery();
        junction = junction.must(fuzzy);

        // Attach an ownership filter.
        User owner = resolveOwnershipFilter(ownerId);
        if (owner != null) {
            Query ownerQuery = builder
                    .keyword()
                    .onField("owner.id")
                    .matching(owner.getId())
                    .createQuery();
            junction.must(ownerQuery);
        }

        FullTextQuery query = getFullTextSession()
                .createFullTextQuery(junction.createQuery(),
                        Application.class);

        return executeQuery(Application.class, query, offset, limit);
    }

    /**
     * Browse the applications in the system.
     *
     * @param offset  The offset of the first applications to fetch.
     * @param limit   The number of data sets to fetch.
     * @param sort    The field on which the records should be sorted.
     * @param order   The sort order, ASC or DESC.
     * @param ownerId An optional user ID to filter by.
     * @return A list of search results.
     */
    @SuppressWarnings({"CPD-END"})
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Browse applications")
    public Response browseApplications(
            @QueryParam(ApiParam.OFFSET_QUERY)
            @DefaultValue(ApiParam.OFFSET_DEFAULT) final int offset,
            @QueryParam(ApiParam.LIMIT_QUERY)
            @DefaultValue(ApiParam.LIMIT_DEFAULT) final int limit,
            @QueryParam(ApiParam.SORT_QUERY)
            @DefaultValue(ApiParam.SORT_DEFAULT) final String sort,
            @QueryParam(ApiParam.ORDER_QUERY)
            @DefaultValue(ApiParam.ORDER_DEFAULT) final SortOrder order,
            @io.swagger.annotations.ApiParam(type = "string")
            @Optional @QueryParam("owner") final BigInteger ownerId) {
        // Validate the incoming owner id.
        User owner = resolveOwnershipFilter(ownerId);

        // Assert that the sort is on a valid column
        Criteria countCriteria = getSession().createCriteria(Application.class);
        countCriteria.setProjection(Projections.rowCount());

        Criteria browseCriteria =
                getSession().createCriteria(Application.class);
        browseCriteria.setFirstResult(offset);
        browseCriteria.setMaxResults(limit);
        browseCriteria.addOrder(SortUtil.order(order, sort));

        if (owner != null) {
            // Boolean switch on the owner ID.
            browseCriteria.add(Restrictions.eq("owner", owner));
            countCriteria.add(Restrictions.eq("owner", owner));
        }

        return ListResponseBuilder.builder()
                .offset(offset)
                .limit(limit)
                .order(order)
                .sort(sort)
                .total(countCriteria.uniqueResult())
                .addResult(browseCriteria.list())
                .build();
    }

    /**
     * Returns a specific application.
     *
     * @param id The Unique Identifier for the application.
     * @return A response with the application that was requested.
     */
    @GET
    @Path("/{id: [a-f0-9]{32}}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Read application")
    public Response getResource(
            @io.swagger.annotations.ApiParam(type = "string")
            @PathParam("id") final BigInteger id) {
        Application application = getSession().get(Application.class, id);
        assertCanAccess(application, getAdminScope());
        return Response.ok(application).build();
    }

    /**
     * Create an application.
     *
     * @param application The application to create.
     * @return A response with the application that was created.
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Create application")
    public Response createResource(final Application application) {
        // Validate that the ID is empty.
        if (application == null) {
            throw new BadRequestException();
        }
        if (application.getId() != null) {
            throw new BadRequestException();
        }

        // Only admins can change the owner.
        if (application.getOwner() != null) {
            if (!getSecurityContext().isUserInRole(getAdminScope())
                    && !application.getOwner().equals(getCurrentUser())) {
                throw new BadRequestException();
            }
        } else if (getCurrentUser() == null) {
            throw new BadRequestException();
        } else {
            application.setOwner(getCurrentUser());
        }

        // Save it all.
        Session s = getSession();
        s.save(application);

        // Build the URI of the new resources.
        URI resourceLocation = getUriInfo().getAbsolutePathBuilder()
                .path(ApplicationService.class, "getResource")
                .build(IdUtil.toString(application.getId()));

        return Response.created(resourceLocation).build();
    }

    /**
     * Update an application.
     *
     * @param id          The Unique Identifier for the application.
     * @param application The application to update.
     * @return A response with the application that was updated.
     */
    @PUT
    @Path("/{id: [a-f0-9]{32}}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Update application")
    public Response updateResource(
            @io.swagger.annotations.ApiParam(type = "string")
            @PathParam("id") final BigInteger id,
            final Application application) {
        Session s = getSession();

        // Load the old instance.
        Application currentApp = s.get(Application.class, id);

        assertCanAccess(currentApp, getAdminScope());

        // Additional special case - we cannot modify the kangaroo app itself.
        if (currentApp.equals(getAdminApplication())) {
            throw new ForbiddenException();
        }

        // Make sure the body ID's match
        if (!currentApp.equals(application)) {
            throw new BadRequestException();
        }

        // Make sure we're not trying to change data we're not allowed.
        if (!currentApp.getOwner().equals(application.getOwner())) {
            throw new BadRequestException();
        }

        // Did the role change?
        if (!Objects.equals(currentApp.getDefaultRole(),
                application.getDefaultRole())) {

            // Can't null it if it's already been set.
            if (application.getDefaultRole() == null) {
                throw new BadRequestException();
            }

            // Make sure the new role belongs to this application.
            Role resolveDesired =
                    s.get(Role.class, application.getDefaultRole().getId());
            if (resolveDesired == null || !Objects.equals(
                    resolveDesired.getApplication(), application)) {
                throw new BadRequestException();
            }
        }

        // Transfer all the values we're allowed to edit.
        currentApp.setName(application.getName());

        s.update(currentApp);

        return Response.ok(application).build();
    }

    /**
     * Delete an application.
     *
     * @param id The Unique Identifier for the application.
     * @return A response that indicates the successs of this operation.
     */
    @DELETE
    @Path("/{id: [a-f0-9]{32}}")
    @ApiOperation(value = "Delete application")
    public Response deleteResource(
            @io.swagger.annotations.ApiParam(type = "string")
            @PathParam("id") final BigInteger id) {
        Session s = getSession();
        Application a = s.get(Application.class, id);

        assertCanAccess(a, getAdminScope());

        // Additional special case - we cannot delete the kangaroo app itself.
        if (a.equals(getAdminApplication())) {
            throw new ForbiddenException();
        }

        // Let's hope they now what they're doing.
        s.delete(a);

        return Response.noContent().build();
    }

    /**
     * Return the scope required to access ALL resources on this services.
     *
     * @return A string naming the scope.
     */
    @Override
    protected String getAdminScope() {
        return Scope.APPLICATION_ADMIN;
    }

    /**
     * Return the scope required to access resources on this service.
     *
     * @return A string naming the scope.
     */
    @Override
    protected String getAccessScope() {
        return Scope.APPLICATION;
    }

}
