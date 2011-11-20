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
import java.io.InterruptedIOException;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.trask.sandbox.executors.RetriableCommand;
import com.github.trask.sandbox.http.client.HttpRequest.HttpRequestType;
import com.github.trask.sandbox.http.client.ResponseCallback.OnAbortByUrlProvider;
import com.google.common.util.concurrent.ListenableFuture;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.AsyncHttpClient.BoundRequestBuilder;

/**
 * @author Trask Stalnaker
 */
class HttpCommand implements RetriableCommand<HttpResponse> {

    private static final Logger logger = LoggerFactory.getLogger(HttpCommand.class);

    private final HttpRequest httpRequest;
    private final UrlProvider baseUrlProvider;
    private final ResponseCallback responseCallback;
    private final AsyncHttpClient asyncHttpClient;

    private volatile String baseUrl;

    public HttpCommand(HttpRequest httpRequest, UrlProvider baseUrlProvider,
            ResponseCallback responseCallback, AsyncHttpClient asyncHttpClient) {

        this.httpRequest = httpRequest;
        this.baseUrlProvider = baseUrlProvider;
        this.responseCallback = responseCallback;
        this.asyncHttpClient = asyncHttpClient;
    }

    public ListenableFuture<HttpResponse> execute() throws FailAndBackoffException, AbortException {

        logger.debug("execute(): httpRequest={}", httpRequest);
        String url = getUrl();
        BoundRequestBuilder requestBuilder = buildRequest(url);
        return executeRequest(requestBuilder);
    }

    public ResultType checkResult(HttpResponse response) {
        logger.debug("checkResult(): response.statusCode={}", response.getStatusCode());
        if (responseCallback == null) {
            return ResultType.Success;
        } else if (responseCallback.isValidResponse(response)) {
            return ResultType.Success;
        } else {
            baseUrlProvider.markAsFailed(baseUrl);
            return ResultType.FailureWithBackoff;
        }
    }

    public ExceptionType checkAsyncException(Throwable t) {
        logger.error(t.getMessage(), t);
        logger.debug("shouldBackoff(): exception={}", t);
        baseUrlProvider.markAsFailed(baseUrl);
        return ExceptionType.FailAndBackoff;
    }

    public void onSuccess(HttpResponse response) {
        logger.debug("onSuccess(): response={}", response);
        if (responseCallback != null) {
            responseCallback.onSuccess(response);
        }
    }

    public void onExceedMaxRetries() {
        logger.error("onMaxRetriesExceeded()");
        if (responseCallback != null) {
            responseCallback.onExceedMaxRetries();
        }
    }

    private String getUrl() throws FailAndBackoffException, AbortException {
        try {
            baseUrl = baseUrlProvider.get();
        } catch (AbortException e) {
            if (responseCallback instanceof OnAbortByUrlProvider) {
                ((OnAbortByUrlProvider) responseCallback).onAbortByUrlProvider();
            }
            throw e;
        }
        return baseUrl + httpRequest.getUrl();
    }

    private BoundRequestBuilder buildRequest(String url) {
        BoundRequestBuilder requestBuilder;
        if (httpRequest.getRequestType() == HttpRequestType.Get) {
            requestBuilder = asyncHttpClient.prepareGet(url);
        } else {
            requestBuilder = asyncHttpClient.preparePost(url);
            requestBuilder.setBody(httpRequest.getBody());
        }
        if (httpRequest.getHeaders() != null) {
            for (Entry<String, String> header : httpRequest.getHeaders().entrySet()) {
                requestBuilder.setHeader(header.getKey(), header.getValue());
            }
        }
        return requestBuilder;
    }

    private ListenableFuture<HttpResponse> executeRequest(BoundRequestBuilder requestBuilder)
            throws FailAndBackoffException, AbortException {

        try {
            return new ListenableHttpResponseFuture(requestBuilder.execute());
        } catch (InterruptedIOException e) {
            // probably interrupted as part of shutdown sequence
            throw new AbortException(e);
        } catch (IOException e) {
            throw new FailAndBackoffException(e);
        }
    }
}
