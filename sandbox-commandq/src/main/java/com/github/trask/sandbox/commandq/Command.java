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

/**
 * @author Trask Stalnaker
 */
public interface Command {

    void execute() throws Exception;
    // retry policy applies to executions which complete
    // (whether successfully or unsuccessfully)
    RetryPolicy retryPolicy();
    // executions which exceed the node failure timeout
    // are assumed to have died via node failure
    // and other nodes may pick them up and execute them
    // long lockDurationForExecution();

    // TODO how to reduce failover time to other nodes
    // with larger node failure timeout seconds
    // (e.g. 60 seconds)
    //
    // every 20 seconds while command is executing
    // update the QueuedCommand visibleNextAt to extend
    // the lock the node has on the QueuedCommand
    // **this can be done in batch on all currently executing
    // commands for that node (add nodeId to QueuedCommand)**
    //
    // this shouldn't be overly aggressive since
    // most executions will run very quickly and will never
    // need this. problem though is to guarantee update
    // to visibleNextAt will be timely, but this should be
    // manageable via ScheduledExecutorService delegating
    // to Cached ExecutorService
    //
    // also need to make sweeper smart and when it sees upcoming
    // expirations it should check for them right after expiration
    // so there is little delay for another node to take over
    //
    // goal is to employ chaos monkey in production!!
    // "the only way not to fail is to fail all the time"
    //
    // the problem with Heartbeat collection idea is that is
    // basically a "join" which of course doesn't model so nice
    //
    // revisiting Heartbeat collection idea:
    // * nodeId
    // * aliveAt
    // delete record if no currently executing commands
    // so updates will be only proportional to actual number
    // of commands that exceed 10 seconds
    //
    // other nodes still need to poll Heartbeat table every 10
    // seconds, but then they no longer have to poll
    // QueuedCommand table! Just look for dead nodes
    // then start processing it's QueuedCommands until there
    // are no more, then delete the Heartbeat record for the
    // dead node
    // !!!
    //
    // this also transfers more easily to a more efficient
    // in-memory heartbeat mechanism later on!!
    //
    // need to keep JVM size down to eliminate any possibility
    // of heap fragmentation leading to full GCs (though soon
    // will have JDK7)

}
