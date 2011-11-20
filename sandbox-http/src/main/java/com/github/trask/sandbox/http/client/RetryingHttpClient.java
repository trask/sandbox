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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.trask.sandbox.clock.Clock;
import com.github.trask.sandbox.executors.RetryPolicy;
import com.github.trask.sandbox.executors.RetryingExecutorService;
import com.ning.http.client.AsyncHttpClient;

/**
 * @author Trask Stalnaker
 */
public class RetryingHttpClient {

    private static final Logger logger = LoggerFactory.getLogger(RetryingHttpClient.class);
    private static final UrlProvider EMPTY_BASE_URL_PROVIDER = new EmptyBaseUrlProvider();

    private final RetryingExecutorService retryingExecutorService;
    private final AsyncHttpClient asyncHttpClient;
    private final Clock clock;

    public RetryingHttpClient(RetryingExecutorService retryingExecutorService,
            AsyncHttpClient asyncHttpClient, Clock clock) {

        this.retryingExecutorService = retryingExecutorService;
        this.asyncHttpClient = asyncHttpClient;
        this.clock = clock;
    }

    public void sendHttp(HttpRequest httpRequest, ResponseCallback responseCallback,
            RetryPolicy retryPolicy) {

        sendHttp(httpRequest, EMPTY_BASE_URL_PROVIDER, responseCallback, retryPolicy);
    }

    public void sendHttp(HttpRequest httpRequest, UrlProvider baseUrlProvider,
            ResponseCallback responseCallback, RetryPolicy retryPolicy) {

        logger.debug("sendHttp(): httpRequest.relativeUrl={}", httpRequest.getUrl());
        HttpCommand command = new HttpCommand(httpRequest, baseUrlProvider, responseCallback,
                asyncHttpClient);
        retryingExecutorService.submit(command, retryPolicy);
    }

    public void sendComet(HttpRequest httpRequest, UrlProvider baseUrlProvider,
            ResponseCallback responseCallback, RetryPolicy retryPolicy) {

        logger.debug("sendComet(): httpRequest.relativeUrl={}", httpRequest.getUrl());
        CometCommand command = new CometCommand(httpRequest, baseUrlProvider, responseCallback,
                asyncHttpClient, clock);
        retryingExecutorService.submit(command, retryPolicy);
    }
}
