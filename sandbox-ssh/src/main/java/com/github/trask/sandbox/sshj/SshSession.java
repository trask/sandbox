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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.NoSuchAlgorithmException;

import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.connection.channel.direct.Session;
import net.schmizz.sshj.connection.channel.direct.Session.Command;
import net.schmizz.sshj.xfer.LocalDestFile;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.TeeOutputStream;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Charsets;

/**
 * @author Trask Stalnaker
 */
public class SshSession {

    private static final Logger logger = LoggerFactory.getLogger(SshSession.class);

    private final SSHClient client;

    public SshSession(SSHClient client) {
        this.client = client;
    }

    public String exec(String command) throws IOException {
        logger.debug("exec(\"{}\")", command);
        Session session = client.startSession();
        try {
            session.allocateDefaultPTY();
            Command cmd = session.exec(command);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            IOUtils.copy(cmd.getInputStream(), new TeeOutputStream(out, new LoggingOutputStream()));
            return new String(out.toByteArray(), Charsets.UTF_8.name());
        } finally {
            session.close();
        }
    }

    // this is useful for commands which return tons and tons of lines
    // and could result in OutOfMemoryError if they are all captured locally
    public void execVoid(String command) throws IOException {
        logger.debug("exec(\"{}\")", command);
        Session session = client.startSession();
        try {
            session.allocateDefaultPTY();
            Command cmd = session.exec(command);
            IOUtils.copy(cmd.getInputStream(), new LoggingOutputStream());
        } finally {
            session.close();
        }
    }

    // looks for resource first on classpath and then in filesystem
    public void scp(String resource) throws IOException, NoSuchAlgorithmException {
        logger.debug("scp(\"{}\")", resource);
        InputStream in = SshSession.class.getClassLoader().getResourceAsStream(resource);
        if (in == null) {
            File file = new File(resource);
            if (file.exists()) {
                client.newSCPFileTransfer().upload(file.getPath(), file.getName());
            } else {
                throw new IllegalArgumentException("could not find resource '" + resource + "'");
            }
        } else {
            File tempFile = File.createTempFile("scp", ".tmp");
            IOUtils.copy(in, new FileOutputStream(tempFile));
            String fileName = resource;
            if (fileName.contains("/")) {
                fileName = StringUtils.substringAfterLast(fileName, "/");
            }
            client.newSCPFileTransfer().upload(tempFile.getPath(), fileName);
        }
    }

    public byte[] scpDownload(String remotePath) throws IOException {
        InMemoryLocalDestFile dest = new InMemoryLocalDestFile();
        client.newSCPFileTransfer().download(remotePath, dest);
        return dest.getContent();
    }

    public void disconnect() throws IOException {
        client.disconnect();
    }

    private static class InMemoryLocalDestFile implements LocalDestFile {
        private final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        public byte[] getContent() {
            return baos.toByteArray();
        }
        public LocalDestFile getChild(String name) {
            throw new UnsupportedOperationException();
        }
        public OutputStream getOutputStream() throws IOException {
            return baos;
        }
        public LocalDestFile getTargetDirectory(String dirname) throws IOException {
            if (".".equals(dirname)) {
                return this;
            } else {
                throw new UnsupportedOperationException();
            }
        }
        public LocalDestFile getTargetFile(String filename) throws IOException {
            return this;
        }
        public void setLastAccessedTime(long t) throws IOException {
            // method from LocalDestFile interface that is not needed here
        }
        public void setLastModifiedTime(long t) throws IOException {
            // method from LocalDestFile interface that is not needed here
        }
        public void setPermissions(int perms) throws IOException {
            // method from LocalDestFile interface that is not needed here
        }
    }

    private static class LoggingOutputStream extends OutputStream {
        private final ByteArrayOutputStream holder = new ByteArrayOutputStream();
        private volatile boolean closed = false;
        @Override
        public void close() {
            flush();
            closed = true;
        }
        @Override
        public void write(final int b) throws IOException {
            if (closed) {
                throw new IOException("The stream has been closed.");
            }
            holder.write(b);
            if (b == '\n') {
                // should cover both \r\n and \n line terminators
                flush();
            }
        }
        @Override
        public void flush() {
            logger.debug(holder.toString().trim());
            holder.reset();
        }
    }
}
