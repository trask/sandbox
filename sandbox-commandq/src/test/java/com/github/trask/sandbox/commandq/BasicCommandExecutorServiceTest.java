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
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;

import org.bson.types.ObjectId;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;

/**
 * @author Trask Stalnaker
 */
public class BasicCommandExecutorServiceTest {

    private QueuedCommandDao queuedCommandDao;
    private MockCommandService mockCommandService;
    private BasicCommandExecutorService commandExecutorService;

    @Before
    public void before() {
        queuedCommandDao = mock(QueuedCommandDao.class);
        mockCommandService = new MockCommandService();
        Injector injector = Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {
                bind(MockCommandService.class).toInstance(mockCommandService);
            }
        });
        commandExecutorService = new BasicCommandExecutorService(queuedCommandDao, injector);
    }

    @Test
    public void shouldLockAndInsertThenExecuteThenDeleteOnSuccessfulInitialRun() throws Exception {
        // given
        ObjectId id = new ObjectId();
        Command command = mock(Command.class);
        given(queuedCommandDao.lockAndInsert(command)).willReturn(id);
        // when
        commandExecutorService.queueAndRunCommand(command);
        // then
        InOrder inOrder = inOrder(queuedCommandDao, command);
        inOrder.verify(queuedCommandDao).lockAndInsert(eq(command));
        inOrder.verify(command).execute();
        inOrder.verify(queuedCommandDao).delete(eq(id));
    }

    @Test
    public void shouldLockThenExecuteThenDeleteOnSuccessfulRetry() throws Exception {
        // given
        ObjectId id = new ObjectId();
        Command command = mock(Command.class);
        QueuedCommand queuedCommand = new QueuedCommand();
        queuedCommand.setId(id);
        queuedCommand.setCommand(command);
        queuedCommand.setNFailures(1);
        // when
        commandExecutorService.runQueuedCommand(queuedCommand);
        // then
        InOrder inOrder = inOrder(queuedCommandDao, command);
        inOrder.verify(queuedCommandDao).lock(eq(id));
        inOrder.verify(command).execute();
        inOrder.verify(queuedCommandDao).delete(eq(id));
    }

    @Test
    public void shouldInjectCommandOnInitialRun() {
        // given
        ObjectId id = new ObjectId();
        MockCommand command = new MockCommand();
        given(queuedCommandDao.lockAndInsert(command)).willReturn(id);
        // when
        commandExecutorService.queueAndRunCommand(command);
        // then
        assertThat(command.getCommandService(), is(mockCommandService));
    }

    @Test
    public void shouldInjectCommandOnRetry() {
        // given
        ObjectId id = new ObjectId();
        MockCommand command = new MockCommand();
        QueuedCommand queuedCommand = new QueuedCommand();
        queuedCommand.setId(id);
        queuedCommand.setCommand(command);
        queuedCommand.setNFailures(1);
        // when
        commandExecutorService.runQueuedCommand(queuedCommand);
        // then
        assertThat(command.getCommandService(), is(mockCommandService));
    }
}
