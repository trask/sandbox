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
package com.github.trask.sandbox.mail.mock;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.github.trask.sandbox.mail.MailService;

/**
 * Consumers of this project will want to mock out the mail service for unit testing so this is made
 * available as a part of the core library.
 * 
 * @author Trask Stalnaker
 */
public class MockMailService implements MailService {

    private final List<MailEntry> mailEntries = new ArrayList<MailEntry>();

    public Future<Void> sendMail(String from, String to, String subject, String htmlBody,
            String textBody) {

        mailEntries.add(new MailEntry(from, to, subject, htmlBody, textBody));
        return new MockVoidFuture();
    }

    public List<MailEntry> getMailEntries() {
        return mailEntries;
    }

    public static class MailEntry {
        private final String from;
        private final String to;
        private final String subject;
        private final String htmlBody;
        private final String textBody;
        public MailEntry(String from, String to, String subject, String htmlBody, String textBody) {
            this.from = from;
            this.to = to;
            this.subject = subject;
            this.htmlBody = htmlBody;
            this.textBody = textBody;
        }
        public String getFrom() {
            return from;
        }
        public String getTo() {
            return to;
        }
        public String getSubject() {
            return subject;
        }
        public String getHtmlBody() {
            return htmlBody;
        }
        public String getTextBody() {
            return textBody;
        }
    }

    private static final class MockVoidFuture implements Future<Void> {
        public boolean cancel(boolean mayInterruptIfRunning) {
            return false;
        }
        public boolean isCancelled() {
            return false;
        }
        public boolean isDone() {
            return true;
        }
        public Void get() throws InterruptedException, ExecutionException {
            return null;
        }
        public Void get(long timeout, TimeUnit unit) throws InterruptedException,
                ExecutionException, TimeoutException {
            return null;
        }
    }
}
