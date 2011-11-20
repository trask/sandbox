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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.mail.BodyPart;
import javax.mail.FetchProfile;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.URLName;
import javax.mail.internet.MimeMultipart;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.mail.pop3.POP3SSLStore;

/**
 * Much of this class was copied from
 * http://www.java-tips.org/other-api-tips/javamail/connecting-gmail-using-pop3-connection
 * -with-ssl-6.html
 * 
 * @author Trask Stalnaker
 */
public class PopGmailClient {

    private static final Logger logger = LoggerFactory.getLogger(PopGmailClient.class);

    private final String username;
    private final String password;

    public PopGmailClient(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public List<PoppedEmail> pop() throws IOException, MessagingException {

        logger.debug("pop()");

        String SSL_FACTORY = "javax.net.ssl.SSLSocketFactory";

        Properties pop3Props = new Properties();

        pop3Props.setProperty("mail.pop3.socketFactory.class", SSL_FACTORY);
        pop3Props.setProperty("mail.pop3.socketFactory.fallback", "false");
        pop3Props.setProperty("mail.pop3.port", "995");
        pop3Props.setProperty("mail.pop3.socketFactory.port", "995");

        URLName url = new URLName("pop3", "pop.gmail.com", 995, "", username, password);

        Session session = Session.getInstance(pop3Props, null);
        Store store = new POP3SSLStore(session, url);
        store.connect();

        Folder folder = store.getDefaultFolder().getFolder("INBOX");

        folder.open(Folder.READ_WRITE);

        Message[] messages = folder.getMessages();

        FetchProfile fetchProfile = new FetchProfile();
        fetchProfile.add(FetchProfile.Item.ENVELOPE);
        folder.fetch(messages, fetchProfile);

        List<PoppedEmail> poppedEmails = new ArrayList<PoppedEmail>();
        for (Message message : messages) {
            String from = message.getFrom()[0].toString();
            String subject = message.getSubject();

            if (message.getContent() instanceof String) {
                poppedEmails
                        .add(new PoppedEmail(subject, from, null, (String) message.getContent()));
            } else {
                MimeMultipart mmp = (MimeMultipart) message.getContent();
                if (mmp.getCount() == 1 && mmp.getBodyPart(0).getContent() instanceof MimeMultipart) {
                    // emails sent out via GmailMailService have this format
                    mmp = (MimeMultipart) mmp.getBodyPart(0).getContent();
                }
                String textBody = null;
                String htmlBody = null;
                for (int i = 0; i < mmp.getCount(); i++) {
                    BodyPart bodyPart = mmp.getBodyPart(i);
                    if (bodyPart.getContentType().startsWith("text/plain")) {
                        textBody = (String) bodyPart.getContent();
                        logger.debug("pop(): found text/plain content: {}", textBody);
                    } else if (bodyPart.getContentType().startsWith("text/html")) {
                        htmlBody = (String) bodyPart.getContent();
                        logger.debug("pop(): found text/html content: {}", htmlBody);
                    }
                }
                poppedEmails.add(new PoppedEmail(subject, from, htmlBody, textBody));
            }
            message.setFlag(Flags.Flag.DELETED, true);
        }

        folder.close(true);
        store.close();

        return poppedEmails;
    }

    public static class PoppedEmail {

        private final String from;
        private final String subject;
        private final String htmlBody;
        private final String textBody;

        public PoppedEmail(String from, String subject, String htmlBody, String textBody) {
            this.from = from;
            this.subject = subject;
            this.htmlBody = htmlBody;
            this.textBody = textBody;
        }
        public String getFrom() {
            return from;
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
}
