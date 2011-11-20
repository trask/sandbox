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
package com.github.trask.sandbox.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.trask.sandbox.clock.Clock;
import com.github.trask.sandbox.mongodb.BasicDao;
import com.google.code.morphia.AdvancedDatastore;
import com.google.code.morphia.DatastoreImpl;
import com.google.code.morphia.query.Query;
import com.google.code.morphia.query.UpdateOperations;

/**
 * @author Trask Stalnaker
 */
public class RegisteredMessageHandlerDao extends BasicDao {

    private static final Logger logger = LoggerFactory.getLogger(RegisteredMessageHandlerDao.class);

    private final String nodeUrl;
    private final Clock clock;

    public RegisteredMessageHandlerDao(String nodeUrl, AdvancedDatastore datastore, Clock clock) {

        super(datastore, RegisteredMessageHandler.class);
        this.nodeUrl = nodeUrl;
        this.clock = clock;
        ((DatastoreImpl) getDatastore()).getMapper().addMappedClass(RegisteredMessageHandler.class);
    }

    public void insert(String id) {
        logger.debug("insert(): id={}", id);
        RegisteredMessageHandler registeredMessageHandler = new RegisteredMessageHandler();
        registeredMessageHandler.setId(id);
        registeredMessageHandler.setNodeUrl(nodeUrl);
        registeredMessageHandler.setConnectedAt(clock.currentTimeMillis());
        getDatastore().insert(registeredMessageHandler);
    }

    public void upsert(String id) {
        logger.debug("upsert(): id={}", id);
        UpdateOperations<RegisteredMessageHandler> updateOperations = getDatastore()
                .createUpdateOperations(RegisteredMessageHandler.class)
                .set("nodeUrl", nodeUrl)
                .set("connectedAt", clock.currentTimeMillis());
        Query<RegisteredMessageHandler> updateQuery = getDatastore()
                .createQuery(RegisteredMessageHandler.class)
                .filter("id =", id);
        getDatastore().update(updateQuery, updateOperations, true);
    }

    public String getNodeUrl(String id) {
        logger.debug("getNodeUrl(): id={}", id);
        RegisteredMessageHandler registeredMessageHandler =
                getDatastore().get(RegisteredMessageHandler.class, id);
        return (registeredMessageHandler == null) ? null : registeredMessageHandler.getNodeUrl();
    }

    public void delete(String id) {
        logger.debug("delete(): id={}", id);
        getDatastore().delete(RegisteredMessageHandler.class, id);
    }
}
