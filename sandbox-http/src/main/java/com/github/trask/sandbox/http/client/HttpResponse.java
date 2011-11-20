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

import com.google.common.base.Objects;
import com.ning.http.client.FluentCaseInsensitiveStringsMap;

/**
 * @author Trask Stalnaker
 */
public class HttpResponse {

    private final int statusCode;
    private final FluentCaseInsensitiveStringsMap headers;
    private final String body;

    public HttpResponse(int statusCode, FluentCaseInsensitiveStringsMap headers, String body) {
        this.statusCode = statusCode;
        this.headers = headers;
        this.body = body;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public FluentCaseInsensitiveStringsMap getHeaders() {
        return headers;
    }

    public String getHeader(String name) {
        return headers.getFirstValue(name);
    }

    public String getBody() {
        return body;
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("statusCode", statusCode)
                .add("headers", headers)
                .add("body", body)
                .toString();
    }
}
