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

import java.security.AccessController;
import java.security.PrivilegedAction;

import javax.servlet.Filter;
import javax.servlet.Servlet;
import javax.servlet.ServletContextListener;

import com.github.trask.sandbox.isolation.IsolatedClassLoader;
import com.github.trask.sandbox.isolation.IsolatedWeavingClassLoader;
import com.github.trask.sandbox.jetty.impl.Dummy404Servlet;
import com.github.trask.sandbox.jetty.impl.InnerServletServer;
import com.github.trask.sandbox.jetty.impl.InnerServletServerBridge;

/**
 * @author Trask Stalnaker
 */
public final class IsolatedServletServer {

    private final InnerServletServerBridge innerServletServer;

    private IsolatedServletServer(ClassLoader classLoader) {
        try {
            innerServletServer = (InnerServletServerBridge) Class.forName(
                    InnerServletServer.class.getName(), true, classLoader).newInstance();
        } catch (InstantiationException e) {
            throw new IllegalStateException(e);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException(e);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(e);
        }
    }

    public void setPort(int port) {
        innerServletServer.setPort(port);
    }

    public void setContextPath(String contextPath) {
        innerServletServer.setContextPath(contextPath);
    }

    public void addListener(Class<? extends ServletContextListener> listenerClass) {
        innerServletServer.addListener(listenerClass.getName());
    }

    public void addFilter(Class<? extends Filter> filterClass, String path) {
        innerServletServer.addFilter(filterClass.getName(), path);
    }

    public void addServlet(Class<? extends Servlet> servletClass, String path) {
        innerServletServer.addServlet(servletClass.getName(), path);
    }

    // this is convenient for backing a filter with a dummy servlet because
    // jetty won't process a request if there is no associated servlet
    public void addDummy404Servlet(String path) {
        innerServletServer.addServlet(Dummy404Servlet.class.getName(), path);
    }
    public void setInitParameter(String name, String value) {
        innerServletServer.setInitParameter(name, value);
    }

    public void start() throws Exception {
        innerServletServer.start();
    }

    public void stop() throws Exception {
        innerServletServer.stop();
    }

    public static IsolatedServletServer newServer() {
        ClassLoader isolatedClassLoader = getIsolatedClassLoader();
        return new IsolatedServletServer(isolatedClassLoader);
    }

    public static IsolatedServletServer newWeavingServer() {
        ClassLoader isolatedWeavingClassLoader = getIsolatedWeavingClassLoader();
        return new IsolatedServletServer(isolatedWeavingClassLoader);
    }

    private static ClassLoader getIsolatedClassLoader() {
        return AccessController.doPrivileged(new PrivilegedAction<ClassLoader>() {
            public ClassLoader run() {
                return new IsolatedClassLoader(InnerServletServerBridge.class);
            }
        });
    }

    private static ClassLoader getIsolatedWeavingClassLoader() {
        return AccessController.doPrivileged(new PrivilegedAction<ClassLoader>() {
            public ClassLoader run() {
                return new IsolatedWeavingClassLoader(InnerServletServerBridge.class);
            }
        });
    }
}
