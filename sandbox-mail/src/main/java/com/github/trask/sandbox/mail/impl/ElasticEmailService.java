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

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.trask.sandbox.mail.MailService;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.AsyncHttpClient.BoundRequestBuilder;
import com.ning.http.client.Response;

/**
 * @author Trask Stalnaker
 */
public class ElasticEmailService implements MailService {

    private static final Logger logger = LoggerFactory.getLogger(ElasticEmailService.class);

    private final String elasticEmailUsername;
    private final String elasticEmailApiKey;
    private final AsyncHttpClient asyncHttpClient;

    public ElasticEmailService(String elasticEmailUsername, String elasticEmailApiKey,
            AsyncHttpClient asyncHttpClient) {

        this.elasticEmailUsername = elasticEmailUsername;
        this.elasticEmailApiKey = elasticEmailApiKey;
        this.asyncHttpClient = asyncHttpClient;
    }

    public Future<Void> sendMail(String from, String to, String subject, String htmlBody,
            String textBody) {

        logger.debug("sendMail(): from={}", from);
        logger.debug("sendMail(): to={}", to);
        logger.debug("sendMail(): subject={}", subject);
        logger.debug("sendMail(): htmlBody={}", htmlBody);
        logger.debug("sendMail(): textBody={}", textBody);

        BoundRequestBuilder builder =
                asyncHttpClient.preparePost("https://api.elasticemail.com/mailer/send");
        builder.addParameter("username", elasticEmailUsername);
        builder.addParameter("api_key", elasticEmailApiKey);
        builder.addParameter("from", from);
        // TODO test if name can be encoded in from above,
        // e.g. "John Smith" <john.smith@example.com>
        // builder.addParameter("from_name", fromName);
        builder.addParameter("to", to);
        builder.addParameter("subject", subject);
        builder.addParameter("body_html", htmlBody);
        builder.addParameter("body_text", textBody);

        try {
            return new VoidFutureWrapper(builder.execute());
        } catch (final IOException e) {
            return new ExceptionThrowingFutureVoid(e);
        }
    }

    private static final class VoidFutureWrapper implements Future<Void> {
        private final Future<Response> future;
        private VoidFutureWrapper(Future<Response> future) {
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
            Response response = future.get();
            verifyResponse(response);
            return null;
        }
        public Void get(long timeout, TimeUnit unit) throws InterruptedException,
                ExecutionException, TimeoutException {
            Response response = future.get(timeout, unit);
            verifyResponse(response);
            return null;
        }
        private void verifyResponse(Response response) {
            try {
                String responseBody = response.getResponseBody().trim();
                if (!responseBody.matches("[0-9a-f-]+")) {
                    throw new IllegalStateException(responseBody);
                }
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        }
    }
}
