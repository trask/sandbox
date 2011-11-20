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
package com.github.trask.sandbox.messaging;

import java.util.Map;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableMap;

/**
 * @author Trask Stalnaker
 */
public class Message {

    private static final Map<String, String> TYPED_EMPTY_MAP = ImmutableMap.of();

    private final Map<String, String> headers;
    private final String body;

    public Message(String body) {
        this(TYPED_EMPTY_MAP, body);
    }

    public Message(Map<String, String> headers) {
        this(headers, "");
    }

    public Message(Map<String, String> headers, String body) {
        // assert args not null
        this.headers = headers;
        this.body = body;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public String getBody() {
        return body;
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("headers", headers)
                .add("body", body)
                .toString();
    }
}
