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

import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.trask.sandbox.commandq.QueuedCommandDao.CouldNotLockForExecutionException;
import com.google.inject.Injector;

/**
 * @author Trask Stalnaker
 */
public class BasicCommandExecutorService {

    private static final Logger logger = LoggerFactory.getLogger(BasicCommandExecutorService.class);

    private final QueuedCommandDao queuedCommandDao;
    private final Injector injector;

    public BasicCommandExecutorService(QueuedCommandDao queuedCommandDao, Injector injector) {
        this.queuedCommandDao = queuedCommandDao;
        this.injector = injector;
    }

    public void queueAndRunCommand(Command command) {
        logger.debug("queueAndRunCommand(): command={}", command);
        ObjectId id = queuedCommandDao.lockAndInsert(command);
        runCommand(command, id, 0);
    }

    public void runQueuedCommand(QueuedCommand queuedCommand) {
        logger.debug("runQueuedCommand(): queuedCommand={}", queuedCommand);
        ObjectId id = queuedCommand.getId();
        try {
            queuedCommandDao.lock(id);
        } catch (CouldNotLockForExecutionException e) {
            // assume another node got to it first
            return;
        }
        runCommand(queuedCommand.getCommand(), id, queuedCommand.getNFailures());
    }

    private void runCommand(Command command, ObjectId id, int nFailures) {
        try {
            injector.injectMembers(command);
            command.execute();
            queuedCommandDao.delete(id);
        } catch (Throwable t) {
            RetryPolicy retryPolicy = command.retryPolicy();
            if (nFailures >= retryPolicy.getMaxRetries()) {
                queuedCommandDao.delete(id);
            } else {
                double delayInSeconds = retryPolicy.getMinBackoffSeconds() * Math.pow(2, nFailures);
                delayInSeconds = Math.min(delayInSeconds, retryPolicy.getMinBackoffSeconds());
                queuedCommandDao.rescheduleOnFailure(id, (long) delayInSeconds);
            }
        }
    }
}