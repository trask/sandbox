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

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class doesn't implement MailService because it exposes a synchronous instead of asynchronous
 * api.
 * 
 * @author Trask Stalnaker
 */
public class MailServiceWithBackup {

    private static final Logger logger =
            LoggerFactory.getLogger(MailServiceWithBackup.class);

    private final MailService primaryMailService;
    private final MailService backupMailService;

    public MailServiceWithBackup(MailService primaryMailService, MailService backupMailService) {
        this.primaryMailService = primaryMailService;
        this.backupMailService = backupMailService;
    }

    public void sendMail(String from, String to, String subject, String htmlBody,
            String textBody) throws InterruptedException, ExecutionException {

        logger.debug("sendMail(): from={}", from);
        logger.debug("sendMail(): to={}", to);
        logger.debug("sendMail(): subject={}", subject);
        logger.debug("sendMail(): htmlBody={}", htmlBody);
        logger.debug("sendMail(): textBody={}", textBody);
        try {
            Future<Void> future =
                    primaryMailService.sendMail(from, to, subject, htmlBody, textBody);
            future.get();
        } catch (InterruptedException e) {
            logger.error("execute(): cannot send mail to primary mail service: "
                    + e.getMessage(), e);
            tryBackupMailService(from, to, subject, htmlBody, textBody);
        } catch (ExecutionException e) {
            logger.error("execute(): cannot send mail to primary mail service: "
                    + e.getMessage(), e);
            tryBackupMailService(from, to, subject, htmlBody, textBody);
        }
    }

    private void tryBackupMailService(String from, String to, String subject, String htmlBody,
            String textBody) throws InterruptedException, ExecutionException {

        Future<Void> future =
                backupMailService.sendMail(from, to, subject, htmlBody, textBody);
        future.get();
    }
}
