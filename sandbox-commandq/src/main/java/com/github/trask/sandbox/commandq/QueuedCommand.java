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

import org.bson.types.ObjectId;

import com.google.code.morphia.annotations.Entity;
import com.google.code.morphia.annotations.Id;
import com.google.code.morphia.annotations.Index;
import com.google.code.morphia.annotations.Indexed;
import com.google.code.morphia.annotations.Indexes;

/**
 * @author Trask Stalnaker
 */
@Entity(noClassnameStored = true)
@Indexes(@Index("appId, visibleNextAt"))
public class QueuedCommand {

    @Id
    private ObjectId id;
    private Command command;
    private String appId;
    @Indexed
    private String lockedByNodeId;
    private int nFailures;
    private long visibleNextAt;

    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public Command getCommand() {
        return command;
    }

    public void setCommand(Command command) {
        this.command = command;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getLockedByNodeId() {
        return lockedByNodeId;
    }

    public void setLockedByNodeId(String lockedByNodeId) {
        this.lockedByNodeId = lockedByNodeId;
    }

    public int getNFailures() {
        return nFailures;
    }

    public void setNFailures(int nFailures) {
        this.nFailures = nFailures;
    }

    public long getVisibleNextAt() {
        return visibleNextAt;
    }

    public void setVisibleNextAt(long visibleNextAt) {
        this.visibleNextAt = visibleNextAt;
    }
}
