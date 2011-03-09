/*
 * Copyright 2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.gradle.api.internal.artifacts.ivyservice;

import org.apache.ivy.core.module.id.ModuleRevisionId;
import org.apache.ivy.plugins.version.AbstractVersionMatcher;

/**
 * @author Hans Dockter
 */
public class SnapshotVersionMatcher extends AbstractVersionMatcher {
    public SnapshotVersionMatcher() {
        super("snapshot");
    }

    public boolean isDynamic(ModuleRevisionId askedMrid) {
        return askedMrid.getRevision().endsWith("-SNAPSHOT");
    }

    public boolean accept(ModuleRevisionId askedMrid, ModuleRevisionId foundMrid) {
        return askedMrid.getRevision().equals(foundMrid.getRevision());
    }
}
