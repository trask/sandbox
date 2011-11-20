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
package com.github.trask.sandbox.mail.impl;

import java.util.Collections;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceAsync;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceAsyncClient;
import com.amazonaws.services.simpleemail.model.Body;
import com.amazonaws.services.simpleemail.model.Content;
import com.amazonaws.services.simpleemail.model.Destination;
import com.amazonaws.services.simpleemail.model.Message;
import com.amazonaws.services.simpleemail.model.SendEmailRequest;
import com.amazonaws.services.simpleemail.model.SendEmailResult;
import com.github.trask.sandbox.mail.MailService;

/**
 * @author Trask Stalnaker
 */
public class AmazonMailService implements MailService {

    private static final Logger logger = LoggerFactory.getLogger(AmazonMailService.class);

    private final AmazonSimpleEmailServiceAsync simpleEmailServiceAsync;

    public AmazonMailService(String accessKey, String secretKey, ExecutorService executorService) {
        AWSCredentials emailCredentials = new BasicAWSCredentials(accessKey, secretKey);
        simpleEmailServiceAsync =
                new AmazonSimpleEmailServiceAsyncClient(emailCredentials, executorService);
    }

    public Future<Void> sendMail(String from, String to, String subject, String htmlBody,
            String textBody) {

        logger.debug("sendMail(): from={}", from);
        logger.debug("sendMail(): to={}", to);
        logger.debug("sendMail(): subject={}", subject);
        logger.debug("sendMail(): htmlBody={}", htmlBody);
        logger.debug("sendMail(): textBody={}", textBody);

        Body body = new Body(new Content(textBody)).withHtml(new Content(htmlBody));
        Message message = new Message(new Content(subject), body);

        SendEmailRequest sendEmailRequest = new SendEmailRequest();
        sendEmailRequest.setSource(from);
        sendEmailRequest.setDestination(new Destination(Collections.singletonList(to)));
        sendEmailRequest.setMessage(message);
        Future<SendEmailResult> future = simpleEmailServiceAsync.sendEmailAsync(sendEmailRequest);
        return new VoidFutureWrapper(future);
    }

    private static final class VoidFutureWrapper implements Future<Void> {
        private final Future<SendEmailResult> future;
        private VoidFutureWrapper(Future<SendEmailResult> future) {
            this.future = future;
        }
        public boolean cancel(boolean mayInterruptIfRunning) {
            return future.cancel(mayInterruptIfRunning);
        }
        public boolean isCancelled() {
            return future.isCancelled();
        }
        public boolean isDone() {
            return future.isDone();
        }
        public Void get() throws InterruptedException, ExecutionException {
            future.get();
            return null;
        }
        public Void get(long timeout, TimeUnit unit) throws InterruptedException,
                ExecutionException, TimeoutException {
            future.get(timeout, unit);
            return null;
        }
    }
}
