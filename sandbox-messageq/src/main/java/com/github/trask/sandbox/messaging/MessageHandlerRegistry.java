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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Trask Stalnaker
 */
public class MessageHandlerRegistry {

    private static final Logger logger = LoggerFactory.getLogger(MessageHandlerRegistry.class);

    private final RegisteredMessageHandlerDao registeredMessageHandlerDao;

    private final Map<String, MessageHandler> localMessageHandlers =
            new ConcurrentHashMap<String, MessageHandler>();

    public MessageHandlerRegistry(RegisteredMessageHandlerDao registeredMessageHandlerDao) {
        this.registeredMessageHandlerDao = registeredMessageHandlerDao;
    }

    public String register(MessageHandler messageHandler) {
        String id = ObjectId.get().toStringMongod();
        logger.debug("register(): generated messageHandlerId={}", id);
        localMessageHandlers.put(id, messageHandler);
        registeredMessageHandlerDao.insert(id);
        return id;
    }

    public void register(String messageHandlerId, MessageHandler messageHandler) {
        logger.debug("register(): messageHandlerId={}", messageHandlerId);
        // TODO if message handler id is preset on remote node
        // then somehow remove it from remote node's localMessageHandlers
        MessageHandler previousMessageHandler = localMessageHandlers.remove(messageHandlerId);
        if (previousMessageHandler != null) {
            previousMessageHandler.onRemovedFromRegistry();
        }
        localMessageHandlers.put(messageHandlerId, messageHandler);
        registeredMessageHandlerDao.upsert(messageHandlerId);
    }

    public void send(String messageHandlerId, Message message) {
        logger.debug("send(): messageHandlerId={}, message={}", messageHandlerId, message);
        MessageHandler localMessageHandler = localMessageHandlers.remove(messageHandlerId);
        if (localMessageHandler == null) {
            logger.debug("send(): local message handler not found, sending to remote node");
            sendToNodeUrl(messageHandlerId);
        } else {
            logger.debug("send(): local message handler found");
            registeredMessageHandlerDao.delete(messageHandlerId);
            localMessageHandler.onMessage(message);
            // TODO catch MessageHandlerOfflineException and retry
        }
    }

    public int numLocalMessageHandlers() {
        return localMessageHandlers.size();
    }

    private void sendToNodeUrl(String messageHandlerId) {
        String nodeUrl = registeredMessageHandlerDao.getNodeUrl(messageHandlerId);
        if (nodeUrl == null) {
            // uh oh
            // retry in x seconds
        } else {
            // send message to node
        }
    }
}