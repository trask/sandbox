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

import com.google.common.util.concurrent.ListenableFuture;

/**
 * @author Trask Stalnaker
 */
public interface RetriableCommand<E> {

    ListenableFuture<E> execute() throws FailButResetBackoffException,
            FailAndBackoffException, AbortException;
    ResultType checkResult(E result);
    ExceptionType checkAsyncException(Throwable t);
    void onSuccess(E result);
    void onExceedMaxRetries();

    enum ResultType {
        Success, FailureWithoutBackoff, FailureWithBackoff;
    }

    enum ExceptionType {
        Abort, FailButResetBackoff, FailAndBackoff;
    }

    @SuppressWarnings("serial")
    // public is redundant, but eclipse isn't finding these nested classes without it
    public class AbortException extends Exception {
        public AbortException(Throwable cause) {
            super(cause);
        }
    }

    @SuppressWarnings("serial")
    public class FailButResetBackoffException extends Exception {}

    @SuppressWarnings("serial")
    public class FailAndBackoffException extends Exception {
        public FailAndBackoffException(Throwable cause) {
            super(cause);
        }
    }
}
