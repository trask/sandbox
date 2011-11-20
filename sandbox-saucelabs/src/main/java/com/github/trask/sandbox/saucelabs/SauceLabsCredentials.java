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

import org.apache.commons.lang.StringUtils;

/**
 * @author Trask Stalnaker
 */
public class SauceLabsCredentials {

    private final String sauceLabsUsername;
    private final String sauceLabsApiKey;

    public SauceLabsCredentials(String sauceLabsUsername, String sauceLabsApiKey) {
        this.sauceLabsUsername = sauceLabsUsername;
        this.sauceLabsApiKey = sauceLabsApiKey;
    }

    public String getUsername() {
        return sauceLabsUsername;
    }

    public String getApiKey() {
        return sauceLabsApiKey;
    }

    public static SauceLabsCredentials fromSystemEnv() {
        String username = System.getenv("SAUCE_LABS_USERNAME");
        String apiKey = System.getenv("SAUCE_LABS_API_KEY");
        if (StringUtils.isEmpty(username)) {
            throw new IllegalStateException("Missing environment variable SAUCE_LABS_USERNAME");
        }
        if (StringUtils.isEmpty(apiKey)) {
            throw new IllegalStateException("Missing environment variable SAUCE_LABS_API_KEY");
        }
        return new SauceLabsCredentials(username, apiKey);
    }
}
