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
package com.github.trask.sandbox.saucelabs;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.saucelabs.sauceconnect.SauceConnect;

/**
 * @author Trask Stalnaker
 */
public class SauceConnectProcessManager {

    private static final Logger logger = LoggerFactory.getLogger(SauceConnectProcessManager.class);

    private final SauceLabsCredentials sauceLabsCredentials;
    private volatile Process process;
    private volatile Thread readerThread;
    private volatile boolean ready;
    private final Object readyLock = new Object();

    public SauceConnectProcessManager(SauceLabsCredentials sauceLabsCredentials) {
        this.sauceLabsCredentials = sauceLabsCredentials;
    }

    public void start() throws IOException, URISyntaxException {
        logger.debug("start()");
        String javaExecutable = getJavaExecutable();
        File sauceConnectJarFile = getSauceConnectJarFile();
        logger.debug("start(): javaExecutable={}", javaExecutable);
        logger.debug("start(): sauceConnectJarFile.absolutePath={}",
                sauceConnectJarFile.getAbsolutePath());
        ProcessBuilder pb = new ProcessBuilder(javaExecutable, "-jar",
                sauceConnectJarFile.getAbsolutePath(), sauceLabsCredentials.getUsername(),
                sauceLabsCredentials.getApiKey());
        pb.redirectErrorStream();
        process = pb.start();
        readerThread = new Thread(new ProcessReader(process.getInputStream()),
                "saucelabs-reader");
        readerThread.setDaemon(false);
        readerThread.start();
    }

    public void waitForReady() throws InterruptedException {
        logger.debug("waitForReady()");
        synchronized (readyLock) {
            if (ready) {
                logger.debug("waitForReady(): already ready");
                return;
            } else {
                logger.debug("waitForReady(): waiting for notify ...");
                readyLock.wait();
                logger.debug("waitForReady(): notified");
            }
        }
    }

    public void stop() throws InterruptedException {
        logger.debug("stop()");
        readerThread.interrupt();
        process.destroy();
        process.waitFor();
    }

    private String getJavaExecutable() {
        String javaHome = System.getProperty("java.home");
        return javaHome + "/bin/java";
    }

    private File getSauceConnectJarFile() throws URISyntaxException {
        return new File(SauceConnect.class.getProtectionDomain()
                .getCodeSource().getLocation().toURI());
    }

    private class ProcessReader implements Runnable {
        private final InputStream in;
        private ProcessReader(InputStream in) {
            this.in = in;
        }
        public void run() {
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            while (true) {
                String line;
                try {
                    line = reader.readLine();
                } catch (IOException e) {
                    logger.error(e.getMessage(), e);
                    return;
                }
                if (line == null) {
                    // EOF
                    return;
                }
                logger.debug("run(): {}", line);
                if (line.contains("SSH Connected. You may start your tests.")) {
                    synchronized (readyLock) {
                        ready = true;
                        readyLock.notifyAll();
                    }
                }
            }
        }
    }
}
