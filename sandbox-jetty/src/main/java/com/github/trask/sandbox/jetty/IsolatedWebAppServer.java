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

import com.github.trask.sandbox.isolation.IsolatedClassLoader;
import com.github.trask.sandbox.isolation.IsolatedWeavingClassLoader;
import com.github.trask.sandbox.jetty.impl.InnerWebAppServer;
import com.github.trask.sandbox.jetty.impl.InnerWebAppServerBridge;

/**
 * @author Trask Stalnaker
 */
public final class IsolatedWebAppServer {

    private InnerWebAppServerBridge innerWebAppServer;

    private IsolatedWebAppServer(ClassLoader classLoader) {
        try {
            innerWebAppServer = (InnerWebAppServerBridge) Class.forName(
                    InnerWebAppServer.class.getName(), true, classLoader).newInstance();
        } catch (InstantiationException e) {
            throw new IllegalStateException(e);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException(e);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(e);
        }
    }

    public void setPort(int port) {
        innerWebAppServer.setPort(port);
    }

    public void setDescriptor(String descriptor) {
        innerWebAppServer.setDescriptor(descriptor);
    }

    public void setResourceBase(String resourceBase) {
        innerWebAppServer.setResourceBase(resourceBase);
    }

    public void setContextPath(String contextPath) {
        innerWebAppServer.setContextPath(contextPath);
    }

    public void setInitParameter(String name, String value) {
        innerWebAppServer.setInitParameter(name, value);
    }

    public void start() throws Exception {
        innerWebAppServer.start();
    }

    public void stop() throws Exception {
        innerWebAppServer.stop();
    }

    public static IsolatedWebAppServer newServer() {
        ClassLoader isolatedClassLoader = getIsolatedClassLoader();
        return new IsolatedWebAppServer(isolatedClassLoader);
    }

    public static IsolatedWebAppServer newWeavingServer() {
        ClassLoader isolatedWeavingClassLoader = getIsolatedWeavingClassLoader();
        return new IsolatedWebAppServer(isolatedWeavingClassLoader);
    }

    private static ClassLoader getIsolatedClassLoader() {
        return AccessController.doPrivileged(new PrivilegedAction<ClassLoader>() {
            public ClassLoader run() {
                return new IsolatedClassLoader(InnerWebAppServerBridge.class);
            }
        });
    }

    private static ClassLoader getIsolatedWeavingClassLoader() {
        return AccessController.doPrivileged(new PrivilegedAction<ClassLoader>() {
            public ClassLoader run() {
                return new IsolatedWeavingClassLoader(InnerWebAppServerBridge.class);
            }
        });
    }
}
