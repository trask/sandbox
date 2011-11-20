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

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.github.trask.sandbox.http.server.HttpServer;
import com.github.trask.sandbox.http.server.MockHttpHandler;
import com.github.trask.sandbox.http.server.MockHttpHandler.With;
import com.github.trask.sandbox.testing.ThreadChecker;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.AsyncHttpClient.BoundRequestBuilder;
import com.ning.http.client.Response;

/**
 * @author Trask Stalnaker
 */
public class HttpClientModuleTest {

    private static final int PORT = 8091;

    private Set<Thread> preExistingThreads;
    private MockHttpHandler mockHttpHandler;
    private HttpServer httpServer;
    private AsyncHttpClient asyncHttpClient;

    @Before
    public void before() {
        preExistingThreads = ThreadChecker.currentThreadList();
        mockHttpHandler = new MockHttpHandler();
        httpServer = new HttpServer(PORT, mockHttpHandler);
        asyncHttpClient = new AsyncHttpClientProvider().get();
    }

    @After
    public void after() throws InterruptedException {
        asyncHttpClient.close();
        httpServer.stop();
        ThreadChecker.postShutdownThreadCheck(preExistingThreads);
    }

    @Test
    public void shouldSendAndClose() throws Exception {
        // given
        mockHttpHandler.uri("/ab").willRespond(With.content(""));
        // when
        BoundRequestBuilder requestBuilder =
                asyncHttpClient.prepareGet("http://localhost:" + PORT + "/ab");
        requestBuilder.execute().get();
        // then
        assertThat(mockHttpHandler.getRequestCount("/ab"), is(1));
    }

    @Test
    public void shouldReceiveMessage() throws Exception {
        // given
        mockHttpHandler.uri("/a/b").willRespond(With.content("hello back to you"));
        // when
        BoundRequestBuilder requestBuilder =
                asyncHttpClient.prepareGet("http://localhost:" + PORT + "/a/b");
        Response response = requestBuilder.execute().get();
        // then
        assertThat(response.getResponseBody(), is("hello back to you"));
    }
}
