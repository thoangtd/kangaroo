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

package net.krotscheck.kangaroo.test.rule.database;

import org.h2.tools.DeleteDbFiles;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Create a new test database using the H2 driver.
 *
 * @author Michael Krotscheck
 */
public final class H2TestDatabase extends AbstractTestDatabase
        implements ITestDatabase {

    /**
     * The database connection that keeps this instance alive.
     */
    private Connection connection;

    /**
     * Create a new test database.
     *
     * @return A reference to the database, which you must close manually.
     */
    @Override
    public ITestDatabase create() {
        deleteDatabase(getJdbcConnectionString());
        connection = getConnection();
        return this;
    }

    /**
     * The name for the created database.
     *
     * @return The username.
     */
    @Override
    public String getName() {
        String url = getJdbcConnectionString();
        return url.substring(url.lastIndexOf(':'));
    }

    /**
     * Get an autocloseable instance of a database connection. You should
     * close this yourself.
     *
     * @return The connection string.
     */
    @Override
    public Connection getConnection() {
        loadDriver();
        try {
            return DriverManager.getConnection(getJdbcConnectionString(),
                    getUser(), getPassword());
        } catch (SQLException e) {
            throw new RuntimeException("cannot create h2 database", e);
        }
    }

    /**
     * Closes this resource, relinquishing any underlying resources.
     * This method is invoked automatically on objects managed by the
     * {@code try}-with-resources statement.
     *
     * @throws IOException if this resource cannot be closed
     */
    @Override
    public void close() throws IOException {
        try {
            this.connection.close();
            deleteDatabase(getJdbcConnectionString());
        } catch (SQLException sql) {
            throw new IOException(sql);
        }
    }

    /**
     * Helper method, to delete any existing databases.
     *
     * @param jbdcUrl The JDBC Url, tested for protocol.
     */
    private void deleteDatabase(final String jbdcUrl) {

        // Remove the prefix.
        String url = jbdcUrl.substring("jdbc:h2:".length());
        // Split on protocol.
        String[] parts = url.split(":");
        String protocol = parts[0];
        String path = parts[1];

        // Grab the last path segment
        String dir = path.substring(0, path.lastIndexOf('/'));
        String db = path.substring(path.lastIndexOf('/') + 1);

        if ("file".equals(protocol)) {
            // Also delete the file.
            DeleteDbFiles.execute(dir, db, true);
        }
    }
}
