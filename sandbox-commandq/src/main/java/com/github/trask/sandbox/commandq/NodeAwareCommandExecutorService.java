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

import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Trask Stalnaker
 */
public class NodeAwareCommandExecutorService {

    private static final Logger logger =
            LoggerFactory.getLogger(NodeAwareCommandExecutorService.class);

    private final BasicCommandExecutorService commandExecutorService;
    private final QueueNodeDao queueNodeDao;

    private final AtomicInteger lockCount = new AtomicInteger();

    public NodeAwareCommandExecutorService(BasicCommandExecutorService commandExecutorService,
            QueueNodeDao queueNodeDao) {

        this.commandExecutorService = commandExecutorService;
        this.queueNodeDao = queueNodeDao;
    }

    public int getLockCount() {
        int count = lockCount.intValue();
        logger.trace("getLockCount(): count={}", count);
        return count;
    }

    public void queueAndRunCommand(Command command) {
        logger.debug("queueAndRunCommand(): command={}", command);
        incrementLockCount();
        try {
            commandExecutorService.queueAndRunCommand(command);
        } finally {
            decrementLockCount();
        }
    }

    public void runQueuedCommand(QueuedCommand queuedCommand) {
        logger.debug("runQueuedCommand(): queuedCommand={}", queuedCommand);
        incrementLockCount();
        try {
            commandExecutorService.runQueuedCommand(queuedCommand);
        } finally {
            decrementLockCount();
        }
    }

    private void incrementLockCount() {
        synchronized (lockCount) {
            if (lockCount.intValue() == 0) {
                queueNodeDao.updateAliveAtAndLockedCommands(true);
            }
            lockCount.incrementAndGet();
        }
    }

    private void decrementLockCount() {
        synchronized (lockCount) {
            lockCount.decrementAndGet();
            if (lockCount.intValue() == 0) {
                queueNodeDao.updateAliveAtAndLockedCommands(false);
            }
        }
    }
}
