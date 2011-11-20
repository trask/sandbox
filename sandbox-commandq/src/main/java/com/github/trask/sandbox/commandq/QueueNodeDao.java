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
package com.github.trask.sandbox.commandq;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.trask.sandbox.clock.Clock;
import com.github.trask.sandbox.mongodb.BasicDao;
import com.google.code.morphia.AdvancedDatastore;
import com.google.code.morphia.query.Query;
import com.google.code.morphia.query.UpdateOperations;
import com.google.code.morphia.query.UpdateResults;

/**
 * @author Trask Stalnaker
 */
public class QueueNodeDao extends BasicDao {

    private static final Logger logger = LoggerFactory.getLogger(QueueNodeDao.class);

    private final String nodeId;
    private final String appId;
    private final Clock clock;

    public QueueNodeDao(String nodeId, String appId, AdvancedDatastore datastore, Clock clock) {

        super(datastore, QueueNode.class);
        this.nodeId = nodeId;
        this.appId = appId;
        this.clock = clock;
        QueueNode node = new QueueNode();
        node.setNodeId(nodeId);
        node.setAppId(appId);
        node.setLockedCommands(false);
        node.setAliveAt(System.currentTimeMillis());
        getDatastore().insert(node);
    }

    public void updateAliveAtAndLockedCommands(boolean lockedCommands) {
        logger.trace("updateAliveAtAndLockedCommands(): lockedCommands={}", lockedCommands);
        Query<QueueNode> updateQuery = getDatastore().createQuery(QueueNode.class)
                .filter("nodeId =", nodeId);
        UpdateOperations<QueueNode> updateOperations =
                getDatastore().createUpdateOperations(QueueNode.class)
                        .set("aliveAt", clock.currentTimeMillis())
                        .set("lockedCommands", lockedCommands);
        UpdateResults<QueueNode> result =
                getDatastore().updateFirst(updateQuery, updateOperations);
        if (result.getUpdatedCount() != 1) {
            logger.error("update(): could not update queue node with id={}", nodeId);
        }
    }

    public void updateAliveAt() {
        logger.trace("updateAliveAt()");
        Query<QueueNode> updateQuery = getDatastore().createQuery(QueueNode.class)
                .filter("nodeId =", nodeId);
        UpdateOperations<QueueNode> updateOperations =
                getDatastore().createUpdateOperations(QueueNode.class)
                        .set("aliveAt", clock.currentTimeMillis());
        UpdateResults<QueueNode> result =
                getDatastore().updateFirst(updateQuery, updateOperations);
        if (result.getUpdatedCount() != 1) {
            logger.error("update(): could not update queue node with id={}", nodeId);
        }
    }

    public List<QueueNode> readFailedNodesThatHaveLockedCommands(int timeoutSeconds, int limit) {
        logger.trace("readFailedNodesThatHaveLockedCommands(): timeoutSeconds={}", timeoutSeconds);
        logger.trace("readFailedNodesThatHaveLockedCommands(): limit={}", limit);
        Query<QueueNode> query = getDatastore().createQuery(QueueNode.class)
                .filter("appId =", appId)
                .filter("lockedCommands =", true)
                .filter("aliveAt <", clock.currentTimeMillis() - timeoutSeconds * 1000L)
                .limit(limit);
        return query.asList();
    }

    public List<QueueNode> readFailedNodesThatHaveNoLockedCommands(long seconds) {
        logger.trace("readFailedNodesThatHaveNoLockedCommands(): seconds={}", seconds);
        Query<QueueNode> query = getDatastore().createQuery(QueueNode.class)
                .filter("appId =", appId)
                .filter("lockedCommands =", false)
                .filter("aliveAt <", clock.currentTimeMillis() - seconds * 1000);
        return query.asList();
    }

    public void deleteFailedNode(String nodeId) {
        logger.info("deleteFailedNode(): nodeId={}", nodeId);
        getDatastore().delete(QueueNode.class, nodeId);
    }
}
