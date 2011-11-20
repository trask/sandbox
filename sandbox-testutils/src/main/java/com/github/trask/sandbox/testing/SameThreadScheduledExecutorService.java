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
package com.github.trask.sandbox.testing;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.Callable;
import java.util.concurrent.Delayed;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Copied mostly from com.google.common.util.concurrent.MoreExecutors.SameThreadExecutorService.
 * 
 * @author Trask Stalnaker
 */
public class SameThreadScheduledExecutorService extends AbstractExecutorService implements
        ScheduledExecutorService {

    /**
     * Lock used whenever accessing the state variables (runningTasks, shutdown,
     * terminationCondition) of the executor
     */
    private final Lock lock = new ReentrantLock();

    /** Signaled after the executor is shutdown and running tasks are done */
    private final Condition termination = lock.newCondition();

    /*
     * Conceptually, these two variables describe the executor being in one of three states: -
     * Active: shutdown == false - Shutdown: runningTasks > 0 and shutdown == true - Terminated:
     * runningTasks == 0 and shutdown == true
     */
    private int runningTasks = 0;
    private boolean shutdown = false;

    public void execute(Runnable command) {
        executeInternal(command);
    }

    public boolean isShutdown() {
        lock.lock();
        try {
            return shutdown;
        } finally {
            lock.unlock();
        }
    }

    public void shutdown() {
        shutdownInternal();
    }

    public List<Runnable> shutdownNow() {
        shutdownInternal();
        return Collections.emptyList();
    }

    public boolean isTerminated() {
        return isTerminatedInternal();
    }

    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        long nanos = unit.toNanos(timeout);
        lock.lock();
        try {
            for (;;) {
                if (isTerminatedInternal()) {
                    return true;
                } else if (nanos <= 0) {
                    return false;
                } else {
                    nanos = termination.awaitNanos(nanos);
                }
            }
        } finally {
            lock.unlock();
        }
    }

    public ScheduledFuture<?> schedule(Runnable command, long delay, TimeUnit unit) {
        MockScheduledFuture<Object> future = new MockScheduledFuture<Object>(command, null);
        executeInternal(future);
        return future;
    }

    public <V> ScheduledFuture<V> schedule(Callable<V> callable, long delay, TimeUnit unit) {
        MockScheduledFuture<V> future = new MockScheduledFuture<V>(callable);
        executeInternal(future);
        return future;
    }

    public ScheduledFuture<?> scheduleAtFixedRate(Runnable command, long initialDelay, long period,
            TimeUnit unit) {

        MockScheduledFuture<Object> future = new MockScheduledFuture<Object>(command, null);
        executeInternal(future);
        return future;
    }

    public ScheduledFuture<?> scheduleWithFixedDelay(Runnable command, long initialDelay,
            long delay, TimeUnit unit) {

        MockScheduledFuture<Object> future = new MockScheduledFuture<Object>(command, null);
        executeInternal(future);
        return future;
    }

    // these "...Internal" methods are broken out primarily so that we can "mockito spy" on this
    // instance and track number of calls to public methods without the public methods making calls
    // to other public methods (e.g. submit() internally calling execute())
    private void executeInternal(Runnable command) {
        startTask();
        try {
            command.run();
        } finally {
            endTask();
        }
    }

    private void shutdownInternal() {
        lock.lock();
        try {
            shutdown = true;
        } finally {
            lock.unlock();
        }
    }

    private boolean isTerminatedInternal() {
        lock.lock();
        try {
            return shutdown && runningTasks == 0;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Checks if the executor has been shut down and increments the running task count.
     * 
     * @throws RejectedExecutionException
     *             if the executor has been previously shutdown
     */
    private void startTask() {
        lock.lock();
        try {
            if (isShutdown()) {
                throw new RejectedExecutionException("Executor already shutdown");
            }
            runningTasks++;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Decrements the running task count.
     */
    private void endTask() {
        lock.lock();
        try {
            runningTasks--;
            if (isTerminated()) {
                termination.signalAll();
            }
        } finally {
            lock.unlock();
        }
    }

    private static class MockScheduledFuture<V> extends FutureTask<V> implements ScheduledFuture<V> {
        public MockScheduledFuture(Callable<V> callable) {
            super(callable);
        }
        public MockScheduledFuture(Runnable runnable, V result) {
            super(runnable, result);
        }
        public long getDelay(TimeUnit unit) {
            return 0;
        }
        public int compareTo(Delayed o) {
            return (int) (getDelay(TimeUnit.NANOSECONDS) - o.getDelay(TimeUnit.NANOSECONDS));
        }
    }
}
