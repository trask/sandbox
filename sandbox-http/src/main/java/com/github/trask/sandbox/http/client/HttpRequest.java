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

import java.util.Map;

import com.google.common.base.Objects;

/**
 * @author Trask Stalnaker
 */
public class HttpRequest {

    private final HttpRequestType requestType;
    // can be relative if using BaseUrlProvider
    private final String url;
    private final Map<String, String> headers;
    private final String body;

    public HttpRequest(HttpRequestType requestType, String url,
            Map<String, String> headers, String body) {

        this.requestType = requestType;
        this.url = url;
        this.headers = headers;
        this.body = body;
    }

    public HttpRequestType getRequestType() {
        return requestType;
    }

    public String getUrl() {
        return url;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public String getBody() {
        return body;
    }

    public enum HttpRequestType {
        Get, Post;
    }

    public static HttpRequest newHttpGet(String url) {
        return new HttpRequest(HttpRequestType.Get, url, null, null);
    }

    public static HttpRequest newHttpPost(String url, String body) {
        return new HttpRequest(HttpRequestType.Post, url, null, body);
    }

    @Override
    public String toString() {
        if (requestType == HttpRequestType.Get) {
            return Objects.toStringHelper(this)
                    .add("requestType", requestType)
                    .add("relativeUrl", url)
                    .add("headers", headers)
                    .toString();
        } else {
            return Objects.toStringHelper(this)
                    .add("requestType", requestType)
                    .add("relativeUrl", url)
                    .add("headers", headers)
                    .add("body", body)
                    .toString();
        }
    }
}
