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
package com.github.trask.sandbox.saucelabs;

import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.containsString;

import java.io.IOException;
import java.net.InetAddress;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openqa.selenium.WebDriver;

/**
 * @author Trask Stalnaker
 */
public class SauceLabsTest {

    private static final int PORT = 8095;

    private static Server server;
    private static SauceConnectProcessManager sauceConnect;
    private static WebDriver browser;

    @BeforeClass
    public static void beforeClass() throws Exception {
        SauceLabsCredentials sauceLabsCredentials = SauceLabsCredentials.fromSystemEnv();
        server = new Server(PORT);
        server.setHandler(new HelloHandler());
        server.start();
        sauceConnect = new SauceConnectProcessManager(sauceLabsCredentials);
        sauceConnect.start();
        sauceConnect.waitForReady();
        browser = new SauceLabsWebDriverProvider(sauceLabsCredentials)
                .get(SauceLabsTest.class.getName());
    }

    @AfterClass
    public static void afterClass() throws Exception {
        browser.quit();
        sauceConnect.stop();
        server.stop();
    }

    @Test
    public void shouldHitLocalUrl() throws Exception {
        // sauce connect cannot redirect 127.0.0.1 or localhost
        // because some browsers refuse to proxy it
        // (see http://saucelabs.com/docs/sauce-connect-2)
        browser.get("http://" + InetAddress.getLocalHost().getHostName() + ":" + PORT);
        assertThat(browser.getPageSource(), containsString("Hello World"));
    }

    private static class HelloHandler extends AbstractHandler {

        public void handle(String target, Request baseRequest, HttpServletRequest request,
                HttpServletResponse response) throws IOException, ServletException {

            response.setContentType("text/html;charset=utf-8");
            response.setStatus(HttpServletResponse.SC_OK);
            baseRequest.setHandled(true);
            response.getWriter().println("<h1>Hello World</h1>");
        }
    }
}
