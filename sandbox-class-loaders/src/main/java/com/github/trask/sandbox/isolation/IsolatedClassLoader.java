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

/**
 * @author Trask Stalnaker
 */
public class IsolatedClassLoader extends ClassLoader implements ExtensibleClassLoader {

    private final ClassLoaderExtension extension;

    // in some cases an explicit bridge interface isn't needed since all "java."
    // classes can be used as bridge interface / classes
    public IsolatedClassLoader() {
        super(IsolatedClassLoader.class.getClassLoader());
        extension = new ClassLoaderExtension(this);
    }

    public IsolatedClassLoader(Class<?> bridgeInterface) {
        super(IsolatedClassLoader.class.getClassLoader());
        extension = new ClassLoaderExtension(this, bridgeInterface);
    }

    public <S, T extends S> S newInstance(Class<T> implClass, Class<S> bridgeClass)
            throws InstantiationException, IllegalAccessException,
            ClassNotFoundException {

        return bridgeClass.cast(loadClass(implClass.getName()).newInstance());
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        if (extension.isJavaSystemClass(name)) {
            return super.findClass(name);
        } else {
            return extension.findClass(name);
        }
    }

    @Override
    protected synchronized Class<?> loadClass(String name, boolean resolve)
            throws ClassNotFoundException {

        if (extension.isJavaSystemClass(name)) {
            return super.loadClass(name, resolve);
        } else {
            return extension.loadClass(name, resolve);
        }
    }

    public void createPackageIfNecessary(String packageName) {
        if (getPackage(packageName) == null) {
            definePackage(packageName, null, null, null, null, null, null, null);
        }
    }

    public Class<?> defineClass(String name, byte[] b) {
        return super.defineClass(name, b, 0, b.length);
    }
}
