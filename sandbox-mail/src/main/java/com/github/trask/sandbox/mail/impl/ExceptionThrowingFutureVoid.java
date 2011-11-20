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
package com.github.trask.sandbox.mail.impl;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @author Trask Stalnaker
 */
public class ExceptionThrowingFutureVoid implements Future<Void> {

    private final Exception exception;

    public ExceptionThrowingFutureVoid(Exception exception) {
        this.exception = exception;
    }

    public boolean cancel(boolean mayInterruptIfRunning) {
        return false;
    }

    public boolean isCancelled() {
        return false;
    }

    public boolean isDone() {
        return true;
    }

    public Void get() throws InterruptedException, ExecutionException {
        throw new ExecutionException(exception);
    }

    public Void get(long timeout, TimeUnit unit) throws InterruptedException,
            ExecutionException, TimeoutException {
        throw new ExecutionException(exception);
    }
}
