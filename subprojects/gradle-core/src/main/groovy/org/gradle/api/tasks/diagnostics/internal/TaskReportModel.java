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
package org.gradle.api.tasks.diagnostics.internal;

import java.util.Set;

        if (groups.containsKey("") && groups.keySet().size() > 1) {
            groups.putAll("other", groups.get(""));
            groups.removeAll("");
        }
    }

    public static Comparator<String> last(final Comparator<String> comparator, final String lastValue) {
        return new Comparator<String>() {
            public int compare(String o1, String o2) {
                boolean o1Other = o1.equalsIgnoreCase(lastValue);
                boolean o2Other = o2.equalsIgnoreCase(lastValue);
                if (o1Other && o2Other) {
                    return 0;
                }
                if (o1Other && !o2Other) {
                    return 1;
                }
                if (!o1Other && o2Other) {
                    return -1;
                }
                return comparator.compare(o1, o2);
            }
        };

public interface TaskReportModel {
    String DEFAULT_GROUP = "";

    /**
     * Returns the task groups which make up this model, in the order that they should be displayed.
     */
    Set<String> getGroups();

    /**
     * Returns the tasks for the given group, in the order that they should be displayed.
     */
    Set<TaskDetails> getTasksForGroup(String group);
}
