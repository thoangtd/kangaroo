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

package net.krotscheck.kangaroo.authz.oauth2.exception;

import com.google.common.net.HttpHeaders;
import net.krotscheck.kangaroo.authz.common.database.entity.ClientType;
import net.krotscheck.kangaroo.authz.oauth2.exception.RFC6749.InvalidClientException;
import net.krotscheck.kangaroo.common.exception.KangarooException;
import net.krotscheck.kangaroo.common.exception.mapper.KangarooExceptionMapper;
import net.krotscheck.kangaroo.util.HttpUtil;
import org.glassfish.jersey.internal.ExceptionMapperFactory;
import org.glassfish.jersey.internal.inject.Bindings;
import org.glassfish.jersey.internal.inject.InjectionManager;
import org.glassfish.jersey.internal.inject.Injections;
import org.glassfish.jersey.spi.ExceptionMappers;
import org.junit.Test;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;

import static org.junit.Assert.assertEquals;

/**
 * Tests for the redirecting exception mapper.
 *
 * @author Michael Krotscheck
 */
public final class RedirectingExceptionMapperTest {

    /**
     * Test that a regular client puts the error results in the query string.
     *
     * @throws Exception Should not be thrown.
     */
    @Test
    public void testRegularMapping() throws Exception {
        KangarooException ke = new InvalidClientException();
        InjectionManager injector = Injections.createInjectionManager();
        ExceptionMappers mappers = new ExceptionMapperFactory(injector);

        injector.register(new KangarooExceptionMapper.Binder());
        injector.register(new RedirectingExceptionMapper.Binder());
        injector.register(Bindings.service(mappers)
                .to(ExceptionMappers.class));

        RedirectingExceptionMapper mapper =
                injector.getInstance(RedirectingExceptionMapper.class);

        URI redirect = UriBuilder.fromUri("http://redirect.example.com/")
                .build();
        RedirectingException re = new RedirectingException(ke, redirect,
                ClientType.AuthorizationGrant);

        Response r = mapper.toResponse(re);
        assertEquals(302, r.getStatus());

        URI location =
                UriBuilder.fromUri(r.getHeaderString(HttpHeaders.LOCATION))
                        .build();

        assertEquals(location.getScheme(), redirect.getScheme());
        assertEquals(location.getHost(), redirect.getHost());
        assertEquals(location.getPort(), redirect.getPort());
        assertEquals(location.getPath(), redirect.getPath());

        MultivaluedMap<String, String> params =
                HttpUtil.parseQueryParams(location);
        assertEquals(params.getFirst("error"),
                ke.getCode().getError());
        assertEquals(params.getFirst("error_description"),
                ke.getCode().getErrorDescription());
    }

    /**
     * Assert that an implicit client puts the error results in the fragment.
     *
     * @throws Exception Should not be thrown.
     */
    @Test
    public void testImplicitMapping() throws Exception {
        KangarooException ke = new InvalidClientException();
        InjectionManager injector = Injections.createInjectionManager();
        ExceptionMappers mappers = new ExceptionMapperFactory(injector);

        injector.register(new KangarooExceptionMapper.Binder());
        injector.register(new RedirectingExceptionMapper.Binder());
        injector.register(Bindings.service(mappers)
                .to(ExceptionMappers.class));

        RedirectingExceptionMapper mapper =
                injector.getInstance(RedirectingExceptionMapper.class);

        URI redirect = UriBuilder.fromUri("http://redirect.example.com/")
                .build();
        RedirectingException re = new RedirectingException(ke, redirect,
                ClientType.Implicit);

        Response r = mapper.toResponse(re);
        assertEquals(302, r.getStatus());

        URI location =
                UriBuilder.fromUri(r.getHeaderString(HttpHeaders.LOCATION))
                        .build();

        assertEquals(location.getScheme(), redirect.getScheme());
        assertEquals(location.getHost(), redirect.getHost());
        assertEquals(location.getPort(), redirect.getPort());
        assertEquals(location.getPath(), redirect.getPath());

        MultivaluedMap<String, String> params =
                HttpUtil.parseQueryParams(location.getFragment());
        assertEquals(params.getFirst("error"),
                ke.getCode().getError());
        assertEquals(params.getFirst("error_description"),
                ke.getCode().getErrorDescription());

        injector.shutdown();
    }

}
