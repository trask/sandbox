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

/**
 * Isolating bridge interface can only have dependencies on java.* packages.
 * 
 * @author Trask Stalnaker
 */
public interface InnerServletServerBridge {

    public void setPort(int port);
    public void setContextPath(String contextPath);
    public void addListener(String listenerClassName);
    public void addFilter(String filterClassName, String path);
    public void addServlet(String servletClassName, String path);
    public void setInitParameter(String name, String value);
    public void start() throws Exception;
    public void stop() throws Exception;
}
