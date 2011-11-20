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

import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.trask.sandbox.clock.Clock;
import com.github.trask.sandbox.http.client.HttpRequest.HttpRequestType;
import com.google.common.util.concurrent.ListenableFuture;
import com.ning.http.client.AsyncHttpClient;

/**
 * @author Trask Stalnaker
 */
class CometCommand extends HttpCommand {

    private static final Logger logger = LoggerFactory.getLogger(CometCommand.class);

    private final Clock clock;

    private volatile long sentAt;

    public CometCommand(HttpRequest httpRequest, UrlProvider baseUrlProvider,
            ResponseCallback responseCallback, AsyncHttpClient asyncHttpClient, Clock clock) {

        super(httpRequest, baseUrlProvider, responseCallback, asyncHttpClient);
        if (httpRequest.getRequestType() != HttpRequestType.Get) {
            throw new IllegalArgumentException();
        }
        this.clock = clock;
    }

    @Override
    public ListenableFuture<HttpResponse> execute() throws AbortException,
            FailAndBackoffException {

        sentAt = clock.currentTimeMillis();
        return super.execute();
    }

    @Override
    public ExceptionType checkAsyncException(Throwable t) {
        logger.error(t.getMessage(), t);
        logger.debug("checkException(): exception={}", t);
        long resultAt = clock.currentTimeMillis();
        if (resultAt - sentAt < TimeUnit.SECONDS.toMillis(15)) {
            return super.checkAsyncException(t);
        } else {
            // probably just network timeout
            return ExceptionType.FailButResetBackoff;
        }
    }
}
