/**
 * Copyright 2011 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.trask.sandbox.commandq;

/**
 * @author Trask Stalnaker
 */
public class RetryPolicy {

    private final int minBackoffSeconds;
    private final int maxBackoffSeconds;
    private final int maxRetries;

    public RetryPolicy(int minBackoffSeconds, int maxBackoffSeconds, int maxRetries) {
        this.minBackoffSeconds = minBackoffSeconds;
        this.maxBackoffSeconds = maxBackoffSeconds;
        this.maxRetries = maxRetries;
    }

    public int getMinBackoffSeconds() {
        return minBackoffSeconds;
    }

    public int getMaxBackoffSeconds() {
        return maxBackoffSeconds;
    }

    public int getMaxRetries() {
        return maxRetries;
    }
}
