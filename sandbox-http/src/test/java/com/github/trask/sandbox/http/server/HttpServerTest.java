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

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.github.trask.sandbox.testing.ThreadChecker;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.AsyncHttpClient.BoundRequestBuilder;
import com.ning.http.client.Response;

/**
 * @author Trask Stalnaker
 */
public class HttpServerTest {

    private static final int PORT = 8089;

    private Set<Thread> preExistingThreads;
    private HttpServer httpServer;
    private AsyncHttpClient asyncHttpClient;

    @Before
    public void before() {
        preExistingThreads = ThreadChecker.currentThreadList();
        httpServer = new HttpServer(PORT, new EchoHttpHandler());
        asyncHttpClient = new AsyncHttpClient();
    }

    @After
    public void after() throws InterruptedException {
        httpServer.stop();
        asyncHttpClient.close();
        ThreadChecker.postShutdownThreadCheck(preExistingThreads);
    }

    @Test
    public void should() throws Exception {
        BoundRequestBuilder request = asyncHttpClient.preparePost("http://localhost:" + PORT);
        request.setBody("hello there");
        Response response = request.execute().get();
        assertThat(response.getResponseBody(), is("hello there"));
    }
}
