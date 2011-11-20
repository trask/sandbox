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
package com.github.trask.sandbox.mongodb;

import java.net.UnknownHostException;

import com.google.code.morphia.AdvancedDatastore;
import com.google.code.morphia.Morphia;
import com.google.code.morphia.logging.MorphiaLoggerFactory;
import com.google.code.morphia.logging.slf4j.SLF4JLogrImplFactory;
import com.mongodb.Mongo;
import com.mongodb.MongoException;
import com.mongodb.MongoURI;

/**
 * @author Trask Stalnaker
 */
public class MongoDatastoreProvider {

    private static volatile boolean initMorphiaLogging = true;

    private final String uri;
    private final String dbName;

    public MongoDatastoreProvider(String uri, String dbName) {
        this.uri = uri;
        this.dbName = dbName;
    }

    public AdvancedDatastore get() {
        if (initMorphiaLogging) {
            MorphiaLoggerFactory.registerLogger(SLF4JLogrImplFactory.class);
            initMorphiaLogging = false;
        }
        Mongo mongo;
        try {
            mongo = new Mongo(new MongoURI(uri));
        } catch (MongoException e) {
            throw new IllegalStateException(e);
        } catch (UnknownHostException e) {
            throw new IllegalStateException(e);
        }
        Morphia morphia = new Morphia();
        AdvancedDatastore datastore = (AdvancedDatastore) morphia.createDatastore(mongo, dbName);
        datastore.ensureIndexes();
        datastore.ensureCaps();
        return datastore;
    }
}
