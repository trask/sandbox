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

import java.util.EnumSet;
import java.util.concurrent.Callable;

import javax.servlet.Filter;
import javax.servlet.Servlet;
import javax.servlet.ServletContextListener;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.DispatcherType;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.nio.SelectChannelConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;

/**
 * @author Trask Stalnaker
 */
public class InnerServletServer implements InnerServletServerBridge {

    private Server server;
    private ServletContextHandler context;

    public InnerServletServer() {
        Helper.runInClassLoader(new Runnable() {
            public void run() {
                server = new Server();
                context = new ServletContextHandler();
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

    public void addListener(final String listenerClassName) {
        Helper.runInClassLoader(new Runnable() {
            public void run() {
                Class<? extends ServletContextListener> listenerClass;
                try {
                    listenerClass = Class.forName(listenerClassName)
                            .asSubclass(ServletContextListener.class);
                    context.addEventListener(listenerClass.newInstance());
                } catch (ClassNotFoundException e) {
                    throw new IllegalStateException(e);
                } catch (InstantiationException e) {
                    throw new IllegalStateException(e);
                } catch (IllegalAccessException e) {
                    throw new IllegalStateException(e);
                }
            }
        });
    }

    public void addFilter(final String filterClassName, final String path) {
        Helper.runInClassLoader(new Runnable() {
            public void run() {
                try {
                    Class<? extends Filter> filterClass =
                            Class.forName(filterClassName).asSubclass(Filter.class);
                    context.addFilter(filterClass, path, EnumSet.of(DispatcherType.REQUEST));
                } catch (ClassNotFoundException e) {
                    throw new IllegalStateException(e);
                }
            }
        });
    }

    public void addServlet(final String servletClassName, final String path) {
        Helper.runInClassLoader(new Runnable() {
            public void run() {
                try {
                    Class<? extends Servlet> servletClass =
                            Class.forName(servletClassName).asSubclass(Servlet.class);
                    context.addServlet(servletClass, path);
                } catch (ClassNotFoundException e) {
                    throw new IllegalStateException(e);
                }
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
