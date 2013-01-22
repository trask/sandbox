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
package com.github.trask.sandbox.isolation;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import com.google.common.io.Resources;
import com.google.common.reflect.Reflection;

/**
 * @author Trask Stalnaker
 */
class ClassLoaderExtension {

    private final Class<?> bridgeInterface;
    // guarded by 'this'
    private final Map<String, Class<?>> classes = new HashMap<String, Class<?>>();

    private final ExtensibleClassLoader extensibleClassLoader;

    // in some cases an explicit bridge interface isn't needed since all "java."
    // classes can be used as bridge interface / classes
    ClassLoaderExtension(ExtensibleClassLoader extensibleClassLoader) {
        this.extensibleClassLoader = extensibleClassLoader;
        bridgeInterface = null;
    }

    ClassLoaderExtension(ExtensibleClassLoader extensibleClassLoader, Class<?> bridgeInterface) {
        this.extensibleClassLoader = extensibleClassLoader;
        this.bridgeInterface = bridgeInterface;
    }

    Class<?> findClass(String name) throws ClassNotFoundException {
        if (bridgeInterface != null && bridgeInterface.getName().equals(name)) {
            return bridgeInterface;
        }
        String resourceName = name.replace('.', '/') + ".class";
        URL url = extensibleClassLoader.getResource(resourceName);
        if (url == null) {
            throw new ClassNotFoundException(name);
        }
        byte[] b;
        try {
            b = Resources.toByteArray(url);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
        if (name.indexOf('.') != -1) {
            String packageName = Reflection.getPackageName(name);
            extensibleClassLoader.createPackageIfNecessary(packageName);
        }
        try {
            return extensibleClassLoader.defineClass(name, b);
        } catch (IOException e) {
            throw new ClassNotFoundException(name, e);
        }
    }

    boolean isJavaSystemClass(String name) {
        return name.startsWith("java.") || name.startsWith("sun.")
                || name.startsWith("javax.management.");
    }

    synchronized Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        Class<?> c = classes.get(name);
        if (c != null) {
            return c;
        }
        c = findClass(name);
        classes.put(name, c);
        return c;
    }
}
