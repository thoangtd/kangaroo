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

package net.krotscheck.kangaroo.common.jackson.types;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import javax.inject.Singleton;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.util.Calendar;

/**
 * Second-precision deserializer for UTC Calendar timestamps.
 *
 * @author Michael Krotscheck
 */
@Provider
@Singleton
public final class UnixTimestampDeserializer
        extends JsonDeserializer<Calendar> {

    /**
     * Deserialize a JSON value (usually a string) into a byte array.
     *
     * @param p    The JSON parser.
     * @param ctxt The serialization context.
     * @return The value as a byte array.
     * @throws IOException             Not thrown.
     */
    @Override
    public Calendar deserialize(final JsonParser p,
                                final DeserializationContext ctxt)
            throws IOException {
        long timestamp = p.getValueAsLong() * 1000;
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timestamp);
        return calendar;
    }
}
