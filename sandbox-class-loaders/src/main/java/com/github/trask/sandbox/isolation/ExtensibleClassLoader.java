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
import java.io.InputStream;

/**
 * @author Trask Stalnaker
 */
interface ExtensibleClassLoader {

    InputStream getResourceAsStream(String name);
    void createPackageIfNecessary(String packageName);
    Class<?> defineClass(String name, byte[] b) throws IOException;
}
