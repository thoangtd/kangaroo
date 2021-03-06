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
 *
 */

package net.krotscheck.kangaroo.common.hibernate;


import net.krotscheck.kangaroo.common.hibernate.factory.FulltextSearchFactoryFactory;
import net.krotscheck.kangaroo.common.hibernate.factory.FulltextSessionFactory;
import net.krotscheck.kangaroo.common.hibernate.factory.HibernateServiceRegistryFactory;
import net.krotscheck.kangaroo.common.hibernate.factory.HibernateSessionFactory;
import net.krotscheck.kangaroo.common.hibernate.factory.HibernateSessionFactoryFactory;
import net.krotscheck.kangaroo.common.hibernate.factory.PooledDataSourceFactory;
import net.krotscheck.kangaroo.common.hibernate.id.Base16BigIntegerConverterProvider;
import net.krotscheck.kangaroo.common.hibernate.lifecycle.SearchIndexContainerLifecycleListener;
import net.krotscheck.kangaroo.common.hibernate.listener.CreatedUpdatedListener;
import net.krotscheck.kangaroo.common.hibernate.mapper.ConstraintViolationExceptionMapper;
import net.krotscheck.kangaroo.common.hibernate.mapper.HibernateExceptionMapper;
import net.krotscheck.kangaroo.common.hibernate.mapper.PersistenceExceptionMapper;
import net.krotscheck.kangaroo.common.hibernate.mapper.PropertyValueExceptionMapper;
import net.krotscheck.kangaroo.common.hibernate.mapper.QueryExceptionMapper;
import net.krotscheck.kangaroo.common.hibernate.mapper.SearchExceptionMapper;
import net.krotscheck.kangaroo.common.hibernate.migration.LiquibaseMigration;
import net.krotscheck.kangaroo.common.hibernate.transaction.TransactionFilter;

import javax.ws.rs.core.Feature;
import javax.ws.rs.core.FeatureContext;

/**
 * This jersey2 feature will ensure that there is always a hibernate session
 * available in the current request context. It is important to keep mind of
 * scope here: Sessions are scoped by request, while the SessionFactory is
 * scoped as a singleton. In other words, if you need a session in a context
 * lifecycle listener, then you should construct it by injecting the
 * SessionFactory first.
 *
 * @author Michael Krotscheck
 */
public final class HibernateFeature implements Feature {

    /**
     * Register the HibernateFeature with the current application context.
     *
     * @param context The application context.
     * @return Always true.
     */
    @Override
    public boolean configure(final FeatureContext context) {

        // Search index construction
        context.register(new SearchIndexContainerLifecycleListener.Binder());

        // Database maintenance
        context.register(new CreatedUpdatedListener.Binder());
        context.register(new LiquibaseMigration.Binder());

        // Hibernate configuration.
        context.register(new HibernateSessionFactory.Binder());
        context.register(new HibernateSessionFactoryFactory.Binder());
        context.register(new HibernateServiceRegistryFactory.Binder());
        context.register(new FulltextSearchFactoryFactory.Binder());
        context.register(new FulltextSessionFactory.Binder());
        context.register(new PooledDataSourceFactory.Binder());

        // Exception Mappers
        context.register(new QueryExceptionMapper.Binder());
        context.register(new HibernateExceptionMapper.Binder());
        context.register(new ConstraintViolationExceptionMapper.Binder());
        context.register(new PersistenceExceptionMapper.Binder());
        context.register(new PropertyValueExceptionMapper.Binder());
        context.register(new SearchExceptionMapper.Binder());

        // Permit the @Transactional annotation.
        context.register(new TransactionFilter.Binder());

        // Correctly map ID's
        context.register(Base16BigIntegerConverterProvider.class);

        return true;
    }
}
