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
package com.github.trask.sandbox.commandq;

import com.google.code.morphia.annotations.Embedded;
import com.google.inject.Inject;

/**
 * @author Trask Stalnaker
 */
@Embedded
public class MockCommand implements Command {

    private int abc;
    private String xyz;

    private MockCommandService commandService;

    // default constructor needed by morphia
    public MockCommand() {}

    public MockCommand(int abc, String xyz) {
        this.abc = abc;
        this.xyz = xyz;
    }

    public void execute() {}

    public RetryPolicy retryPolicy() {
        return new RetryPolicy(10, 100, 5);
    }

    public int getAbc() {
        return abc;
    }

    public void setAbc(int abc) {
        this.abc = abc;
    }

    public String getXyz() {
        return xyz;
    }

    public void setXyz(String xyz) {
        this.xyz = xyz;
    }

    public MockCommandService getCommandService() {
        return commandService;
    }

    @Inject
    public void setCommandService(MockCommandService commandService) {
        this.commandService = commandService;
    }
}
