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
package com.github.trask.sandbox.testing;

/**
 * @author Trask Stalnaker
 */
public final class VerifyUtil {

    private VerifyUtil() {}

    public static void verifyInTheNextNSeconds(int nSeconds, Verification verification)
            throws InterruptedException {

        AssertionError assertionError = null;
        long startAt = System.currentTimeMillis();
        while (System.currentTimeMillis() - startAt < nSeconds * 1000L) {
            try {
                verification.execute();
                return;
            } catch (AssertionError e) {
                assertionError = e;
            } catch (Exception e) {
                throw new VerificationException(e);
            }
            Thread.sleep(100);
        }
        throw assertionError;
    }

    public interface Verification {
        void execute() throws Exception;
    }

    @SuppressWarnings("serial")
    public static class VerificationException extends RuntimeException {
        public VerificationException(Throwable t) {
            super(t);
        }
    }
}
