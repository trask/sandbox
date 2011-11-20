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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.google.common.base.Objects;
import com.google.common.base.Objects.ToStringHelper;

/**
 * @author Trask Stalnaker
 */
public final class ThreadChecker {

    private ThreadChecker() {}

    public static Set<Thread> currentThreadList() {
        return Thread.getAllStackTraces().keySet();
    }

    public static void postShutdownThreadCheck(Set<Thread> preExistingThreads)
            throws InterruptedException {

        List<Thread> rogueThreads = null;
        // give it 5?? seconds to shutdown threads
        for (int i = 0; i < 500; i++) {
            rogueThreads = new ArrayList<Thread>();
            for (Thread thread : currentThreadList()) {
                if (!preExistingThreads.contains(thread)) {
                    rogueThreads.add(thread);
                }
            }
            if (rogueThreads.isEmpty()) {
                // success
                break;
            }
            // failure, wait a few milliseconds and try again
            Thread.sleep(10);
        }
        if (!rogueThreads.isEmpty()) {
            throw new RogueThreadsException(rogueThreads);
        }
    }

    public static void preShutdownNonDaemonThreadCheck() {
        preShutdownNonDaemonThreadCheck(new ArrayList<Thread>());
    }

    public static void preShutdownNonDaemonThreadCheck(List<Thread> preExistingThreads) {
        List<Thread> rogueThreads = new ArrayList<Thread>();
        for (Thread thread : currentThreadList()) {
            if (thread != Thread.currentThread() && !thread.isDaemon()
                    && !preExistingThreads.contains(thread)) {
                rogueThreads.add(thread);
            }
        }
        if (!rogueThreads.isEmpty()) {
            throw new RogueThreadsException(rogueThreads);
        }
    }

    @SuppressWarnings("serial")
    public static class RogueThreadsException extends RuntimeException {
        private final List<Thread> rogueThreads;
        public RogueThreadsException(List<Thread> rogueThreads) {
            this.rogueThreads = rogueThreads;
        }
        @Override
        public String getMessage() {
            StringBuilder sb = new StringBuilder();
            for (Thread rogueThread : rogueThreads) {
                sb.append(threadToString(rogueThread));
                sb.append("\n");
            }
            return sb.toString();
        }
        private static String threadToString(Thread thread) {
            ToStringHelper toStringHelper = Objects.toStringHelper(thread)
                    .add("name", thread.getName())
                    .add("class", thread.getClass().getName())
                    .add("state", thread.getState());
            for (int i = 0; i < Math.min(30, thread.getStackTrace().length); i++) {
                toStringHelper.add("stackTrace." + i, thread.getStackTrace()[i].getClassName());
            }
            return toStringHelper.toString();
        }
    }
}
