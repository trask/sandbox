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
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Trask Stalnaker
 */
public class CommandQueueService {

    private static final Logger logger = LoggerFactory.getLogger(CommandQueueService.class);

    private static final int NUM_SECONDS_IN_HOUR = 3600;

    private final QueuedCommandDao queuedCommandDao;
    private final QueueNodeDao queueNodeDao;
    private final NodeAwareCommandExecutorService commandExecutorService;

    public CommandQueueService(QueuedCommandDao queuedCommandDao, QueueNodeDao queueNodeDao,
            NodeAwareCommandExecutorService commandExecutorService,
            ScheduledExecutorService scheduledExecutorService) {

        this.queuedCommandDao = queuedCommandDao;
        this.queueNodeDao = queueNodeDao;
        this.commandExecutorService = commandExecutorService;

        scheduledExecutorService.scheduleAtFixedRate(new FailedCommandMonitor(), 0, 30,
                TimeUnit.SECONDS);
        scheduledExecutorService.scheduleAtFixedRate(new NodeWithLocksHeartbeat(), 10, 10,
                TimeUnit.SECONDS);
        scheduledExecutorService.scheduleAtFixedRate(new FailedNodeWithLocksMonitor(), 0, 10,
                TimeUnit.SECONDS);
        scheduledExecutorService.scheduleAtFixedRate(new NodeWithNoLocksHeartbeat(), 0,
                NUM_SECONDS_IN_HOUR, TimeUnit.SECONDS);
        scheduledExecutorService.scheduleAtFixedRate(new FailedNodeWithNoLocksMonitor(), 0,
                NUM_SECONDS_IN_HOUR * 12, TimeUnit.SECONDS);

    }

    public void add(Command command) {
        logger.debug("add(): command={}", command);
        commandExecutorService.queueAndRunCommand(command);
    }

    private void runQueuedCommands(QueuedCommandBatchReader getNext) {
        List<QueuedCommand> next = getNext.nextBatch();
        while (!next.isEmpty()) {
            for (QueuedCommand queuedCommand : next) {
                commandExecutorService.runQueuedCommand(queuedCommand);
            }
            next = getNext.nextBatch();
        }
    }

    private interface QueuedCommandBatchReader {
        List<QueuedCommand> nextBatch();
    }

    private class FailedCommandMonitor implements Runnable {
        public void run() {
            logger.trace("FailedCommandMonitor.run()");
            runQueuedCommands(new QueuedCommandBatchReader() {
                public List<QueuedCommand> nextBatch() {
                    return queuedCommandDao.readVisible(2);
                }
            });
        }
    }

    private class NodeWithLocksHeartbeat implements Runnable {
        public void run() {
            logger.trace("NodeWithLocksHeartbeat.run()");
            if (commandExecutorService.getLockCount() > 0) {
                queueNodeDao.updateAliveAt();
            }
        }
    }

    private class FailedNodeWithLocksMonitor implements Runnable {
        public void run() {
            logger.trace("FailedNodeWithLocksMonitor.run()");
            List<QueueNode> failedNodes = queueNodeDao
                    .readFailedNodesThatHaveLockedCommands(60, 10);
            for (final QueueNode failedNode : failedNodes) {
                runQueuedCommands(new QueuedCommandBatchReader() {
                    public List<QueuedCommand> nextBatch() {
                        return queuedCommandDao.readFromFailedNodeQueue(failedNode.getNodeId(), 10);
                    }
                });
            }
        }
    }

    private class NodeWithNoLocksHeartbeat implements Runnable {
        public void run() {
            logger.trace("NodeWithNoLocksHeartbeat.run()");
            queueNodeDao.updateAliveAt();
        }
    }

    private class FailedNodeWithNoLocksMonitor implements Runnable {
        public void run() {
            logger.trace("FailedNodeWithNoLocksMonitor.run()");
            queueNodeDao.readFailedNodesThatHaveNoLockedCommands(NUM_SECONDS_IN_HOUR * 2);
        }
    }
}
