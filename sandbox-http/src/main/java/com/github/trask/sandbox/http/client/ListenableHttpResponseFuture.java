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
package com.github.trask.sandbox.http.client;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.google.common.util.concurrent.ListenableFuture;
import com.ning.http.client.Response;

/**
 * @author Trask Stalnaker
 */
class ListenableHttpResponseFuture implements ListenableFuture<HttpResponse> {

    private final com.ning.http.client.ListenableFuture<Response> delegate;

    public ListenableHttpResponseFuture(com.ning.http.client.ListenableFuture<Response> delegate) {
        this.delegate = delegate;
    }

    public void addListener(Runnable listener, Executor executor) {
        delegate.addListener(listener, executor);
    }

    public boolean cancel(boolean mayInterruptIfRunning) {
        return delegate.cancel(mayInterruptIfRunning);
    }

    public boolean isCancelled() {
        return delegate.isCancelled();
    }

    public boolean isDone() {
        return delegate.isDone();
    }

    public HttpResponse get() throws InterruptedException, ExecutionException {
        try {
            return toHttpResponse(delegate.get());
        } catch (IOException e) {
            throw new ExecutionException(e);
        }
    }

    public HttpResponse get(long timeout, TimeUnit unit) throws InterruptedException,
            ExecutionException, TimeoutException {

        try {
            return toHttpResponse(delegate.get(timeout, unit));
        } catch (IOException e) {
            throw new ExecutionException(e);
        }
    }

    private static HttpResponse toHttpResponse(Response response) throws IOException {
        return new HttpResponse(response.getStatusCode(), response.getHeaders(),
                response.getResponseBody());
    }
}
