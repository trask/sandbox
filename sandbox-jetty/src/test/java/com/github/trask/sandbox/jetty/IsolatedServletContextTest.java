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
package com.github.trask.sandbox.jetty;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.AsyncHttpClient.BoundRequestBuilder;
import com.ning.http.client.Response;

/**
 * @author Trask Stalnaker
 */
public class IsolatedServletContextTest {

    private static final int PORT = 8098;

    private IsolatedServletServer server;
    private AsyncHttpClient asyncHttpClient;

    @Before
    public void before() throws Exception {
        server = IsolatedServletServer.newServer();
        server.setPort(PORT);
        server.setContextPath("/");
        server.addServlet(TestServlet.class, "/test");
        server.addFilter(TestFilter.class, "/filter");
        server.addDummy404Servlet("/filter");
        server.start();
        asyncHttpClient = new AsyncHttpClient();
    }

    @After
    public void after() throws Exception {
        asyncHttpClient.close();
        server.stop();
    }

    @Test
    public void shouldCallServlet() throws Exception {
        TestServlet.flag = true;
        BoundRequestBuilder request = asyncHttpClient
                .prepareGet("http://localhost:" + PORT + "/test");
        Response response = request.execute().get();
        assertThat(response.getResponseBody(), is("false"));
    }

    @Test
    public void shouldCallFilter() throws Exception {
        TestServlet.flag = true;
        BoundRequestBuilder request = asyncHttpClient
                .prepareGet("http://localhost:" + PORT + "/filter");
        Response response = request.execute().get();
        assertThat(response.getResponseBody(), is("false"));
    }
}
