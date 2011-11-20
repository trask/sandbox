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

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.List;

import org.bson.types.ObjectId;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.github.trask.sandbox.clock.MockClock;
import com.github.trask.sandbox.commandq.QueuedCommandDao.CouldNotLockForExecutionException;
import com.github.trask.sandbox.mongodb.MongoDatastoreProvider;
import com.google.code.morphia.AdvancedDatastore;

/**
 * @author Trask Stalnaker
 */
public class QueuedCommandDaoTest {

    private static final String APP_ID = "unittest/1.0";
    private static final String NODE_ID = "unittest/" + System.currentTimeMillis();

    private MockClock clock;
    private AdvancedDatastore datastore;
    private QueuedCommandDao queuedCommandDao;

    @Before
    public void before() {
        clock = new MockClock();
        datastore = new MongoDatastoreProvider("mongodb://localhost", "testdb").get();
        queuedCommandDao = new QueuedCommandDao(NODE_ID, APP_ID, datastore, clock);
    }

    @After
    public void after() {
        datastore.getMongo().dropDatabase("testdb");
    }

    @Test
    public void shouldLockAndInsert() {
        // given
        MockCommand command = new MockCommand(1, "test");
        // when
        ObjectId commandId = queuedCommandDao.lockAndInsert(command);
        // then
        QueuedCommand queuedCommand = datastore.get(QueuedCommand.class, commandId);
        command = (MockCommand) queuedCommand.getCommand();
        assertThat(command.getAbc(), is(1));
        assertThat(command.getXyz(), is("test"));
        assertThat(queuedCommand.getAppId(), is(APP_ID));
        assertThat(queuedCommand.getLockedByNodeId(), is(NODE_ID));
        assertThat(queuedCommand.getNFailures(), is(0));
        assertThat(queuedCommand.getVisibleNextAt(), is(0L));
    }

    @Test
    public void shouldDelete() throws CouldNotLockForExecutionException {
        // given
        ObjectId commandId = queuedCommandDao.lockAndInsert(new MockCommand(1, "test"));
        // when
        queuedCommandDao.delete(commandId);
        // then
        assertThat(datastore.getCount(QueuedCommand.class), is(0L));
    }

    @Test
    public void shouldRescheduleForFailure() throws CouldNotLockForExecutionException {
        // given
        long mockTimeMillis = clock.updateTime();
        ObjectId commandId = queuedCommandDao.lockAndInsert(new MockCommand(1, "test"));
        // when
        int delayInSeconds = 60;
        queuedCommandDao.rescheduleOnFailure(commandId, delayInSeconds);
        // then
        QueuedCommand queuedCommand = datastore.get(QueuedCommand.class, commandId);
        assertThat(queuedCommand.getVisibleNextAt(), is(mockTimeMillis + delayInSeconds * 1000));
    }

    @Test
    public void shouldReadVisible() {
        // given
        clock.updateTime();
        ObjectId id = queuedCommandDao.lockAndInsert(new MockCommand(1, "test"));
        int delayInSeconds = 60;
        queuedCommandDao.rescheduleOnFailure(id, delayInSeconds);
        clock.forwardTime(delayInSeconds * 1000);
        // when
        List<QueuedCommand> visibleQueuedCommands = queuedCommandDao.readVisible(10);
        // then
        assertThat(visibleQueuedCommands.size(), is(1));
    }

    @Test
    public void shouldNotReadBeforeVisible() {
        // given
        clock.updateTime();
        ObjectId id = queuedCommandDao.lockAndInsert(new MockCommand(1, "test"));
        int delayInSeconds = 60;
        queuedCommandDao.rescheduleOnFailure(id, delayInSeconds);
        clock.forwardTime(delayInSeconds * 1000 - 1);
        // when
        List<QueuedCommand> visibleQueuedCommands = queuedCommandDao.readVisible(10);
        // then
        assertThat(visibleQueuedCommands.size(), is(0));
    }

    @Test
    public void shouldReadVisibleWithLimit() {
        // given
        clock.updateTime();
        int delayInSeconds = 60;
        for (int i = 0; i < 11; i++) {
            ObjectId id = queuedCommandDao.lockAndInsert(new MockCommand(1, "test"));
            queuedCommandDao.rescheduleOnFailure(id, delayInSeconds);
        }
        clock.forwardTime(delayInSeconds * 1000 + 1);
        // when
        List<QueuedCommand> visibleQueuedCommands = queuedCommandDao.readVisible(10);
        // then
        assertThat(visibleQueuedCommands.size(), is(10));
    }

    @Test
    public void shouldLock() throws CouldNotLockForExecutionException {
        // given
        clock.updateTime();
        ObjectId commandId = queuedCommandDao.lockAndInsert(new MockCommand(1, "test"));
        int delayInSeconds = 60;
        queuedCommandDao.rescheduleOnFailure(commandId, delayInSeconds);
        clock.forwardTime(delayInSeconds * 1000);
        // when
        queuedCommandDao.lock(commandId);
        // then
        QueuedCommand queuedCommand = datastore.get(QueuedCommand.class, commandId);
        assertThat(queuedCommand.getLockedByNodeId(), is(NODE_ID));
        assertThat(queuedCommand.getVisibleNextAt(), is(0L));
    }
}
