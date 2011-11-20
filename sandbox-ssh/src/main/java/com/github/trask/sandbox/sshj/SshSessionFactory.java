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
package com.github.trask.sandbox.sshj;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.transport.verification.PromiscuousVerifier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Trask Stalnaker
 */
public final class SshSessionFactory {

    private static final Logger logger = LoggerFactory.getLogger(SshSessionFactory.class);

    private SshSessionFactory() {}

    // the private keys cannot be password protected
    public static SshSession connect(String user, String host, long timeoutMillis,
            String... privateKeyPaths) throws IOException, InterruptedException, TimeoutException {

        long startTime = System.currentTimeMillis();
        try {
            return connect(user, host, privateKeyPaths);
        } catch (IOException e) {
        }
        while (System.currentTimeMillis() - startTime < timeoutMillis) {
            logger.debug("waiting for sshd to start ...");
            Thread.sleep(1000);
            try {
                return connect(user, host, privateKeyPaths);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        throw new TimeoutException();
    }

    private static SshSession connect(String user, String host, String[] privateKeyPaths)
            throws IOException {

        SSHClient client = new SSHClient();
        client.addHostKeyVerifier(new PromiscuousVerifier());
        client.connect(host);
        client.authPublickey(user, privateKeyPaths);
        return new SshSession(client);
    }
}
