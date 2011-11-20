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

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import org.apache.commons.mail.DefaultAuthenticator;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.trask.sandbox.mail.MailService;

/**
 * @author Trask Stalnaker
 */
public class SmtpMailService implements MailService {

    private static final Logger logger = LoggerFactory.getLogger(SmtpMailService.class);

    private final String host;
    private final int port;
    private final boolean tls;
    private final String username;
    private final String password;
    private final ExecutorService executorService;

    public SmtpMailService(String host, int port, boolean tls, String username, String password,
            ExecutorService executorService) {

        this.host = host;
        this.port = port;
        this.tls = tls;
        this.username = username;
        this.password = password;
        this.executorService = executorService;
    }

    public Future<Void> sendMail(String from, String to, String subject, String htmlBody,
            String textBody) {

        logger.debug("sendMail(): from={}", from);
        logger.debug("sendMail(): to={}", to);
        logger.debug("sendMail(): subject={}", subject);
        logger.debug("sendMail(): htmlBody={}", htmlBody);
        logger.debug("sendMail(): textBody={}", textBody);

        return executorService.submit(new SendSmtp(from, to, subject, htmlBody, textBody));
    }

    private final class SendSmtp implements Callable<Void> {

        private final String from;
        private final String to;
        private final String subject;
        private final String htmlBody;
        private final String textBody;

        private SendSmtp(String from, String to, String subject, String htmlBody, String textBody) {
            this.from = from;
            this.to = to;
            this.subject = subject;
            this.htmlBody = htmlBody;
            this.textBody = textBody;
        }

        public Void call() throws EmailException {
            HtmlEmail htmlEmail = new HtmlEmail();
            htmlEmail.setHostName(host);
            htmlEmail.setSmtpPort(port);
            htmlEmail.setAuthenticator(new DefaultAuthenticator(username, password));
            htmlEmail.setTLS(tls);
            htmlEmail.setFrom(from);
            htmlEmail.addTo(to);
            htmlEmail.setSubject(subject);
            htmlEmail.setHtmlMsg(htmlBody);
            htmlEmail.setMsg(textBody);
            htmlEmail.send();
            return null;
        }
    }
}
