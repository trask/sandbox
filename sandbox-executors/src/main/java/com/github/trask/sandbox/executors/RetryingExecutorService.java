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

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Trask Stalnaker
 */
public class RetryingExecutorService {

    private static final Logger logger = LoggerFactory.getLogger(RetryingExecutorService.class);

    private final ExecutorService executorService;
    private final ScheduledExecutorService scheduledExecutorService;

    public RetryingExecutorService(ExecutorService executorService,
            ScheduledExecutorService scheduledExecutorService) {

        this.executorService = executorService;
        this.scheduledExecutorService = scheduledExecutorService;
    }

    public <E> void submit(final RetriableCommand<E> command, RetryPolicy retryPolicy) {

        logger.debug("submit() command={}", command);
        RetryingCommandWrapper<E> retryingCommandWrapper = new RetryingCommandWrapper<E>(command,
                retryPolicy, executorService, scheduledExecutorService);
        executorService.submit(retryingCommandWrapper);
    }
}
