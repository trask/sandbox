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

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.trask.sandbox.executors.RetriableCommand.AbortException;
import com.github.trask.sandbox.executors.RetriableCommand.FailAndBackoffException;
import com.github.trask.sandbox.executors.RetriableCommand.FailButResetBackoffException;
import com.google.common.util.concurrent.ListenableFuture;

/**
 * @author Trask Stalnaker
 */
// package protected, please use RetryingAsyncExecutorService
class RetryingCommandWrapper<E> implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(RetriableCommand.class);

    private final RetriableCommand<E> command;
    private final RetryPolicy retryPolicy;
    private final ExecutorService executorService;
    private final ScheduledExecutorService scheduledExecutorService;

    // volatile since they could be updated and subsequently read by different threads
    private volatile long startTimeMillis;
    private volatile int retryIntervalSeconds;

    public RetryingCommandWrapper(RetriableCommand<E> command, RetryPolicy retryPolicy,
            ExecutorService executorService, ScheduledExecutorService scheduledExecutorService) {

        this.command = command;
        this.retryPolicy = retryPolicy;
        this.executorService = executorService;
        this.scheduledExecutorService = scheduledExecutorService;
        if (retryPolicy != null) {
            retryIntervalSeconds = retryPolicy.getInitialRetryIntervalSeconds();
        }
    }

    public void run() {
        if (startTimeMillis == 0) {
            // mark the start time of the first attempt
            startTimeMillis = System.currentTimeMillis();
        }
        try {
            final ListenableFuture<E> future = command.execute();
            future.addListener(new Runnable() {
                public void run() {
                    try {
                        E result = future.get();
                        handleResult(result);
                    } catch (InterruptedException e) {
                        // we've been interrupted, presumably for a good reason (e.g. shutdown)
                        // so we terminate this command
                    } catch (ExecutionException e) {
                        handleAsyncException(e.getCause());
                    }
                }
            }, executorService);
        } catch (FailButResetBackoffException e) {
            resetBackoffAndReExecuteImmediately();
        } catch (FailAndBackoffException e) {
            rescheduleWithBackoff();
        } catch (AbortException e) {
            // do nothing and allow thread to terminate
        }
    }

    private void handleResult(E result) {
        logger.debug("handleResult(): result={}", result);
        switch (command.checkResult(result)) {
        case Success:
            command.onSuccess(result);
            break;
        case FailureWithoutBackoff:
            resetBackoffAndReExecuteImmediately();
            break;
        case FailureWithBackoff:
            rescheduleWithBackoff();
            break;
        }
    }

    private void handleAsyncException(Throwable t) {
        logger.debug("handleException(): exception={}", t);
        switch (command.checkAsyncException(t)) {
        case Abort:
            break;
        case FailButResetBackoff:
            resetBackoffAndReExecuteImmediately();
            break;
        case FailAndBackoff:
            backoff();
            break;
        }
    }

    private void backoff() {
        if (retryPolicy == null) {
            command.onExceedMaxRetries();
        } else if (isTimeToFail()) {
            // we've exceeded our patience, time to terminate this command without rescheduling
            command.onExceedMaxRetries();
        } else {
            rescheduleWithBackoff();
        }
    }

    private boolean isTimeToFail() {
        int finallyFailAfterSeconds = retryPolicy.getFinallyFailAfterSeconds();
        long totalSeconds = System.currentTimeMillis() - startTimeMillis;
        return finallyFailAfterSeconds > 0 && totalSeconds > finallyFailAfterSeconds;
    }

    private void resetBackoffAndReExecuteImmediately() {
        logger.debug("resetBackoffAndReExecuteImmediately()");
        retryIntervalSeconds = retryPolicy.getInitialRetryIntervalSeconds();
        executorService.submit(this);
    }

    private void rescheduleWithBackoff() {
        logger.debug("rescheduleWithBackoff(): retryIntervalSeconds={}", retryIntervalSeconds);
        int localRetryIntervalSeconds = retryIntervalSeconds;
        retryIntervalSeconds = (int) Math.min(
                retryIntervalSeconds * retryPolicy.getRetryIntervalMultiplier(),
                retryPolicy.getMaxRetryIntervalSeconds());
        logger.debug("rescheduleWithBackoff(): backing off {} seconds", localRetryIntervalSeconds);
        scheduledExecutorService.schedule(this, localRetryIntervalSeconds,
                TimeUnit.SECONDS);
    }
}
