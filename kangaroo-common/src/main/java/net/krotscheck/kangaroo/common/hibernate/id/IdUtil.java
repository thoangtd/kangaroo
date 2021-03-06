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

package net.krotscheck.kangaroo.common.hibernate.id;

import com.google.common.base.Strings;
import org.apache.commons.lang3.StringUtils;

import java.math.BigInteger;
import java.security.SecureRandom;

/**
 * A small utility that generates positive database identifiers.
 *
 * @author Michael Krotscheck
 */
public final class IdUtil {

    /**
     * The # of bytes in the ID. OSSTP recommends 128 bits, which means 16
     * bytes.
     */
    private static final int BIT_COUNT = 128;

    /**
     * Random number generator.
     */
    private static final SecureRandom RND = new SecureRandom();

    /**
     * Utility class, private constructor.
     */
    private IdUtil() {
    }

    /**
     * Convert an ID, represented as a string, to a BigInteger id.
     *
     * @param idString The string.
     * @return The BigInteger representation of that ID.
     */
    public static BigInteger fromString(final String idString) {
        if (Strings.isNullOrEmpty(idString)) {
            return null;
        }
        return new BigInteger(idString, 16);
    }

    /**
     * Convert a bigInteger to an string ID representation.
     *
     * @param id The ID to convert.
     * @return The string representation of this id.
     */
    public static String toString(final BigInteger id) {
        if (id == null) {
            return null;
        }
        return StringUtils.leftPad(id.toString(16), 32, "0");
    }

    /**
     * Generate an ID.
     *
     * @return An ID generated from the SecureRandom stream of bytes.
     */
    public static BigInteger next() {
        return new BigInteger(BIT_COUNT - 1, RND);
    }
}
