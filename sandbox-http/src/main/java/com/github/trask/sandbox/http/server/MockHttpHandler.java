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
package com.github.trask.sandbox.http.server;

import static org.jboss.netty.handler.codec.http.HttpResponseStatus.OK;
import static org.jboss.netty.handler.codec.http.HttpVersion.HTTP_1_1;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpHeaders.Names;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;

import com.google.common.base.Charsets;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

/**
 * @author Trask Stalnaker
 */
public class MockHttpHandler implements HttpHandler {

    private final LoadingCache<String, Queue<HttpResponse>> responseQueue =
            CacheBuilder.newBuilder().build(new CacheLoader<String, Queue<HttpResponse>>() {
                @Override
                public Queue<HttpResponse> load(String input) {
                    return new ConcurrentLinkedQueue<HttpResponse>();
                }
            });

    private final LoadingCache<String, BlockingQueue<HttpResponse>> responseBlockingQueue =
            CacheBuilder.newBuilder().build(new CacheLoader<String, BlockingQueue<HttpResponse>>() {
                @Override
                public BlockingQueue<HttpResponse> load(String input) {
                    return new LinkedBlockingQueue<HttpResponse>();
                }
            });

    private final List<String> uris = new ArrayList<String>();

    public MockHttpHandlerStubbing uri(String uri) {
        return new MockHttpHandlerStubbing(uri);
    }

    public HttpResponse handleRequest(HttpRequest request) throws InterruptedException {
        uris.add(request.getUri());
        HttpResponse response = responseQueue.getUnchecked(request.getUri()).poll();
        if (response == null) {
            return responseBlockingQueue.getUnchecked(request.getUri()).take();
        } else {
            return response;
        }
    }

    public int getRequestCount(String uri) {
        return count(uris, uri);
    }

    private int count(List<String> list, String value) {
        int count = 0;
        for (String s : list) {
            if (s.equals(value)) {
                count++;
            }
        }
        return count;
    }

    public class MockHttpHandlerStubbing {
        private final String uri;
        public MockHttpHandlerStubbing(String uri) {
            this.uri = uri;
        }
        public MockHttpHandlerStubbing willRespond(With response) {
            responseQueue.getUnchecked(uri).add(response.toHttpResponse());
            return this;
        }
        public MockHttpHandlerStubbing respondNow(With response) {
            responseBlockingQueue.getUnchecked(uri).add(response.toHttpResponse());
            return this;
        }
    }

    public static final class With {
        private String content;
        private final Map<String, String> headers = new HashMap<String, String>();
        private With() {}
        public static With content(String content) {
            With response = new With();
            response.content = content;
            return response;
        }
        public With header(String name, String value) {
            headers.put(name, value);
            return this;
        }
        HttpResponse toHttpResponse() {
            HttpResponse httpResponse = new DefaultHttpResponse(HTTP_1_1, OK);
            httpResponse.setContent(ChannelBuffers.copiedBuffer(content, Charsets.ISO_8859_1));
            httpResponse.setHeader(Names.CONTENT_LENGTH, content.length());
            for (Entry<String, String> entry : headers.entrySet()) {
                httpResponse.addHeader(entry.getKey(), entry.getValue());
            }
            return httpResponse;
        }
    }
}