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

package net.krotscheck.kangaroo.common.timedtasks;

import java.util.TimerTask;

/**
 * This interface describes a task that should be executed periodically.
 *
 * @author Michael Krotscheck
 */
public interface RepeatingTask {

    /**
     * The task to execute.
     *
     * @return The task to execute.
     */
    TimerTask getTask();

    /**
     * Time in milliseconds between successive task executions.
     *
     * @return Time in milliseconds between successive task executions.
     */
    long getPeriod();

    /**
     * Delay in milliseconds before task is to be executed. By default, this
     * is the interval period, which effectively means that these tasks will
     * never run immediately, but start to tick after the first interval has
     * passed.
     *
     * @return Delay in milliseconds before task is to be executed.
     */
    default long getDelay() {
        return getPeriod();
    }
}
