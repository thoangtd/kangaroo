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

package net.krotscheck.kangaroo.authz.common.database;

import net.krotscheck.kangaroo.common.config.ConfigurationFeature;
import net.krotscheck.kangaroo.common.hibernate.listener.CreatedUpdatedListener;
import net.krotscheck.kangaroo.test.jersey.ContainerTest;
import org.glassfish.jersey.internal.inject.InjectionManager;
import org.glassfish.jersey.server.ResourceConfig;
import org.hibernate.event.spi.PreInsertEventListener;
import org.hibernate.event.spi.PreUpdateEventListener;
import org.junit.Test;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for the database feature.
 *
 * @author Michael Krotscheck
 */
public final class DatabaseFeatureTest extends ContainerTest {

    /**
     * Setup an application.
     *
     * @return A configured application.
     */
    @Override
    protected ResourceConfig createApplication() {
        ResourceConfig a = new ResourceConfig();
        a.register(ConfigurationFeature.class);
        a.register(DatabaseFeature.class);
        a.register(MockService.class);
        return a;
    }

    /**
     * Quick check to see if we can inject the various components of the
     * database feature.
     */
    @Test
    public void testStatus() {
        String response = target("/").request().get(String.class);
        assertEquals("true", response);
    }

    /**
     * A simple endpoint that returns the system status.
     *
     * @author Michael Krotscheck
     */
    @Path("/")
    public static final class MockService {

        /**
         * The system configuration from which to read status features.
         */
        private InjectionManager injector;

        /**
         * Create a new instance of the status service.
         *
         * @param injector injection manager.
         */
        @Inject
        public MockService(final InjectionManager injector) {
            this.injector = injector;
        }

        /**
         * Always returns the version.
         *
         * @return HTTP Response object with the current service status.
         */
        @GET
        @Produces(MediaType.APPLICATION_JSON)
        public Response status() {
            List<PreInsertEventListener> insertListeners = injector
                    .getAllInstances(PreInsertEventListener.class);
            List<PreUpdateEventListener> updateListeners = injector
                    .getAllInstances(PreUpdateEventListener.class);

            assertEquals(1, insertListeners.size());
            assertEquals(1, updateListeners.size());
            assertTrue(CreatedUpdatedListener.class
                    .isInstance(insertListeners.get(0)));
            assertTrue(CreatedUpdatedListener.class
                    .isInstance(updateListeners.get(0)));

            return Response
                    .status(Status.OK)
                    .entity(true)
                    .build();
        }
    }
}
