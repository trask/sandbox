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
package com.github.trask.sandbox.executors;

import com.google.common.base.Objects;

/**
 * @author Trask Stalnaker
 */
public class RetryPolicy {

    public static final RetryPolicy DEFAULT = new RetryPolicy();

    private final int initialRetryIntervalSeconds;
    private final double retryIntervalMultiplier;
    private final int maxRetryIntervalSeconds;
    private final int finallyFailAfterSeconds;

    public RetryPolicy() {
        // default initial interval is 5 seconds
        initialRetryIntervalSeconds = 5;
        // default is doubling
        retryIntervalMultiplier = 2;
        // default max interval is 30 minutes
        maxRetryIntervalSeconds = 1800;
        // default is to never stop trying
        finallyFailAfterSeconds = 0;
    }

    public RetryPolicy(int initialRetryIntervalSeconds, double retryIntervalMultiplier,
            int maxRetryIntervalSeconds, int finallyFailAfterSeconds) {

        this.initialRetryIntervalSeconds = initialRetryIntervalSeconds;
        this.retryIntervalMultiplier = retryIntervalMultiplier;
        this.maxRetryIntervalSeconds = maxRetryIntervalSeconds;
        this.finallyFailAfterSeconds = finallyFailAfterSeconds;
    }

    public int getInitialRetryIntervalSeconds() {
        return initialRetryIntervalSeconds;
    }

    public double getRetryIntervalMultiplier() {
        return retryIntervalMultiplier;
    }

    public int getMaxRetryIntervalSeconds() {
        return maxRetryIntervalSeconds;
    }

    public int getFinallyFailAfterSeconds() {
        return finallyFailAfterSeconds;
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("initialRetryIntervalSeconds", initialRetryIntervalSeconds)
                .add("retryIntervalMultiplier", retryIntervalMultiplier)
                .add("maxRetryIntervalSeconds", maxRetryIntervalSeconds)
                .add("finallyFailAfterSeconds", finallyFailAfterSeconds)
                .toString();
    }
}
