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
package com.github.trask.sandbox.jetty.impl;

import java.util.concurrent.Callable;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.nio.SelectChannelConnector;
import org.eclipse.jetty.webapp.WebAppContext;

/**
 * @author Trask Stalnaker
 */
public class InnerWebAppServer implements InnerWebAppServerBridge {

    private Server server;
    private WebAppContext context;

    public InnerWebAppServer() {
        Helper.runInClassLoader(new Runnable() {
            public void run() {
                server = new Server();
                context = new WebAppContext();
                server.setHandler(context);
            }
        });
    }

    public void setPort(final int port) {
        Helper.runInClassLoader(new Runnable() {
            public void run() {
                Connector connector = new SelectChannelConnector();
                connector.setPort(port);
                server.setConnectors(new Connector[] { connector });
            }
        });
    }

    public void setContextPath(final String contextPath) {
        Helper.runInClassLoader(new Runnable() {
            public void run() {
                context.setContextPath(contextPath);
            }
        });
    }

    public void setDescriptor(final String descriptor) {
        Helper.runInClassLoader(new Runnable() {
            public void run() {
                context.setDescriptor(descriptor);
            }
        });
    }

    public void setResourceBase(final String resourceBase) {
        Helper.runInClassLoader(new Runnable() {
            public void run() {
                context.setResourceBase(resourceBase);
            }
        });
    }

    public void setInitParameter(final String name, final String value) {
        Helper.runInClassLoader(new Runnable() {
            public void run() {
                context.setInitParameter(name, value);
            }
        });
    }

    public void start() throws Exception {
        Helper.runInClassLoader(new Callable<Void>() {
            public Void call() throws Exception {
                server.start();
                return null;
            }
        });
    }

    public void stop() throws Exception {
        Helper.runInClassLoader(new Callable<Void>() {
            public Void call() throws Exception {
                server.stop();
                return null;
            }
        });
    }
}
