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

import org.apache.commons.lang.StringUtils;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.trask.sandbox.clock.Clock;
import com.github.trask.sandbox.mongodb.BasicDao;
import com.google.code.morphia.AdvancedDatastore;
import com.google.code.morphia.DatastoreImpl;
import com.google.code.morphia.Key;
import com.google.code.morphia.query.Query;
import com.google.code.morphia.query.UpdateOperations;
import com.google.code.morphia.query.UpdateResults;
import com.mongodb.WriteResult;

/**
 * @author Trask Stalnaker
 */
public class QueuedCommandDao extends BasicDao {

    private static final Logger logger = LoggerFactory.getLogger(QueuedCommandDao.class);

    private final String nodeId;
    private final String appId;
    private final Clock clock;

    public QueuedCommandDao(String nodeId, String appId, AdvancedDatastore datastore, Clock clock) {

        super(datastore, QueuedCommand.class);
        this.nodeId = nodeId;
        this.appId = appId;
        this.clock = clock;
        ((DatastoreImpl) getDatastore()).getMapper().addMappedClass(QueuedCommand.class);
    }

    public ObjectId lockAndInsert(Command command) {
        QueuedCommand queuedCommand = new QueuedCommand();
        queuedCommand.setAppId(appId);
        queuedCommand.setLockedByNodeId(nodeId);
        queuedCommand.setCommand(command);
        Key<QueuedCommand> key = getDatastore().insert(queuedCommand);
        return (ObjectId) key.getId();
    }

    public void delete(final ObjectId id) {
        Query<QueuedCommand> updateQuery =
                getDatastore().createQuery(QueuedCommand.class).filter("id =", id);
        WriteResult result = getDatastore().delete(updateQuery);
        // hopefully morphia WriteResult will introduce getHadError() like for UpdateResult
        String error = result.getError();
        if (!StringUtils.isEmpty(error)) {
            logger.error("deleteQueuedCommandOnSuccess(): delete error: {}", result.getError());
            // TODO what's the best fallback, if anything?
        }
        if (result.getN() == 0) {
            logger.error("deleteQueuedCommandOnSuccess(): could not find id {} for deletion",
                    id);
        }
    }

    public void rescheduleOnFailure(final ObjectId id, long delayInSeconds) {
        Query<QueuedCommand> updateQuery =
                getDatastore().createQuery(QueuedCommand.class).filter("id =", id);
        UpdateOperations<QueuedCommand> updateOperations =
                getDatastore().createUpdateOperations(QueuedCommand.class)
                        .unset("lockedByNodeId")
                        .inc("nFailures")
                        .set("visibleNextAt", clock.currentTimeMillis() + delayInSeconds * 1000);
        UpdateResults<QueuedCommand> result = getDatastore().update(updateQuery, updateOperations);
        if (result.getHadError()) {
            logger.error("rescheduledQueuedCommandOnFailure(): update error: {}", result.getError());
        }
    }

    public List<QueuedCommand> readVisible(int limit) {
        Query<QueuedCommand> query = getDatastore().createQuery(QueuedCommand.class)
                .filter("appId =", appId)
                .filter("lockedByNodeId =", null)
                .filter("visibleNextAt <=", clock.currentTimeMillis())
                .limit(limit);
        return query.asList();
    }

    public List<QueuedCommand> readFromFailedNodeQueue(String failedNodeId, int limit) {
        Query<QueuedCommand> query = getDatastore().createQuery(QueuedCommand.class)
                .filter("nodeId =", failedNodeId)
                .limit(limit);
        return query.asList();
    }

    public void lock(ObjectId id) throws CouldNotLockForExecutionException {
        Query<QueuedCommand> updateQuery = getDatastore().createQuery(QueuedCommand.class)
                .filter("id =", id)
                .filter("lockedByNodeId =", null)
                .filter("visibleNextAt <=", clock.currentTimeMillis());
        UpdateOperations<QueuedCommand> updateOperations =
                getDatastore().createUpdateOperations(QueuedCommand.class)
                        .set("lockedByNodeId", nodeId)
                        .unset("visibleNextAt");
        UpdateResults<QueuedCommand> result = getDatastore().update(updateQuery, updateOperations);
        if (result.getHadError()) {
            logger.error("lockQueuedCommand(): update error: {}", result.getError());
            throw new CouldNotLockForExecutionException();
        }
        if (result.getUpdatedCount() == 0) {
            throw new CouldNotLockForExecutionException();
        }
    }

    @SuppressWarnings("serial")
    public static class CouldNotLockForExecutionException extends Exception {}
}
