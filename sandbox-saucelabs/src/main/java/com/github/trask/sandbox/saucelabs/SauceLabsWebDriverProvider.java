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

import java.net.MalformedURLException;
import java.net.URL;

import org.openqa.selenium.Platform;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Trask Stalnaker
 */
public class SauceLabsWebDriverProvider {

    private static final Logger logger = LoggerFactory.getLogger(SauceLabsWebDriverProvider.class);

    private final SauceLabsCredentials sauceLabsCredentials;

    public SauceLabsWebDriverProvider(SauceLabsCredentials sauceLabsCredentials) {
        this.sauceLabsCredentials = sauceLabsCredentials;
    }

    public WebDriver get(String testName) {
        logger.debug("get()");
        DesiredCapabilities capabilities =
                new DesiredCapabilities("firefox", "3.6.", Platform.WINDOWS);
        capabilities.setCapability("name", testName);
        URL url;
        try {
            url = new URL("http://" + sauceLabsCredentials.getUsername() + ":"
                    + sauceLabsCredentials.getApiKey() + "@ondemand.saucelabs.com/wd/hub");
        } catch (MalformedURLException e) {
            throw new IllegalStateException(e);
        }
        return new RemoteWebDriver(url, capabilities);
    }
}
