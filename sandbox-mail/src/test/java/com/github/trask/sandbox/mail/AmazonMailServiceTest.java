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
package com.github.trask.sandbox.mail;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import javax.mail.MessagingException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.github.trask.sandbox.mail.PopGmailClient.PoppedEmail;
import com.github.trask.sandbox.mail.impl.AmazonMailService;
import com.github.trask.sandbox.testing.SameThreadScheduledExecutorService;
import com.github.trask.sandbox.testing.ThreadChecker;
import com.github.trask.sandbox.testing.VerifyUtil;
import com.github.trask.sandbox.testing.VerifyUtil.Verification;

/**
 * @author Trask Stalnaker
 */
public class AmazonMailServiceTest {

    private Set<Thread> preExistingThreads;
    private PopGmailClient popClient;
    private ExecutorService executorService;
    private MailService mailService;

    @Before
    public void before() throws Exception {
        preExistingThreads = ThreadChecker.currentThreadList();
        String popGmailUsername = System.getenv("POP_GMAIL_USERNAME");
        String popGmailPassword = System.getenv("POP_GMAIL_PASSWORD");
        popClient = new PopGmailClient(popGmailUsername, popGmailPassword);
        popClient.pop();
        executorService = new SameThreadScheduledExecutorService();
        String awsEmailAccessKey = System.getenv("AWS_EMAIL_ACCESS_KEY");
        String awsEmailSecretKey = System.getenv("AWS_EMAIL_SECRET_KEY");
        mailService = new AmazonMailService(awsEmailAccessKey, awsEmailSecretKey, executorService);
    }

    @After
    public void after() throws InterruptedException {
        executorService.shutdownNow();
        // MultiThreadedHttpConnectionManager.shutdownAll();
        ThreadChecker.postShutdownThreadCheck(preExistingThreads);
    }

    @Test
    public void shouldSendEmail() throws Exception {
        // given
        final long random = new Random().nextLong();
        String from = "unit.test.from@example.com";
        String to = "unit.test.to@example.com";
        String subject = getClass().getSimpleName();
        String htmlBody = "<html><body>click <a href=\"http://google.com\">here</a> " + random;
        String textBody = "go to http://google.com " + random;

        // when
        Future<Void> future = mailService.sendMail(from, to, subject, htmlBody, textBody);
        future.get();

        // then
        VerifyUtil.verifyInTheNextNSeconds(120,
                new Verification() {
                    public void execute() throws IOException, MessagingException {
                        List<PoppedEmail> poppedEmails = popClient.pop();
                        assertThat(poppedEmails.size(), is(1));
                        PoppedEmail poppedEmail = poppedEmails.get(0);
                        assertThat(poppedEmail.getTextBody().contains(Long.toString(random)),
                                is(true));
                    }
                });
    }

    @Test
    public void shouldThrowException() throws Exception {
        // given
        MailService mailService = new AmazonMailService("fail", "fail", executorService);
        String from = "unit.test.from@example.com";
        String to = "unit.test.to@example.com";
        String subject = getClass().getSimpleName();
        String htmlBody = "<html><body>click <a href=\"http://google.com\">here</a>";
        String textBody = "go to http://google.com";

        // when
        Future<Void> future = mailService.sendMail(from, to, subject, htmlBody, textBody);
        boolean exceptionThrown = false;
        try {
            future.get();
        } catch (Exception e) {
            // TODO be more specific about exception
            exceptionThrown = true;
        }

        // then
        assertThat(exceptionThrown, is(true));
    }
}
