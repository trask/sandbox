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

import com.google.code.morphia.annotations.Entity;
import com.google.code.morphia.annotations.Id;
import com.google.code.morphia.annotations.Index;
import com.google.code.morphia.annotations.Indexes;

/**
 * @author Trask Stalnaker
 */
@Entity(noClassnameStored = true)
@Indexes(@Index("appId, working, aliveAt"))
public class QueueNode {

    @Id
    private String nodeId;
    private String appId;
    // has one or more locked commands
    private boolean lockedCommands;
    private long aliveAt;

    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public boolean isLockedCommands() {
        return lockedCommands;
    }

    public void setLockedCommands(boolean lockedCommands) {
        this.lockedCommands = lockedCommands;
    }

    public long getAliveAt() {
        return aliveAt;
    }

    public void setAliveAt(long aliveAt) {
        this.aliveAt = aliveAt;
    }
}
