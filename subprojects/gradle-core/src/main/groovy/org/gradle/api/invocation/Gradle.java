/*
 * Copyright 2009 the original author or authors.
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
package org.gradle.api.invocation;

import groovy.lang.Closure;
import org.gradle.BuildListener;
import org.gradle.StartParameter;
import org.gradle.api.Project;
import org.gradle.api.ProjectEvaluationListener;
import org.gradle.api.execution.TaskExecutionGraph;

import java.io.File;

/**
 * <p>Represents an invocation of Gradle.</p>
 *
 * <p>You can obtain a {@code Gradle} instance by calling {@link Project#getGradle()}.</p>
 */
public interface Gradle {
    /**
     * <p>Returns the current Gradle version.</p>
     *
     * @return The Gradle version. Never returns null.
     */
    String getGradleVersion();

    /**
     * <p>Returns the Gradle user home directory. This directory is used to cache downloaded resources.</p>
     *
     * @return The user home directory. Never returns null.
     */
    File getGradleUserHomeDir();

    /**
     * <p>Returns the Gradle home directory, if any. This directory is the directory containing the Gradle distribution
     * executing this build.</p>
     *
     * @return The home directory. May return null.
     * @deprecated No replacement
     */
    @Deprecated
    File getGradleHomeDir();

    /**
     * <p>Returns the parent build of this build, if any.</p>
     *
     * @return The parent build. May return null.
     */
    Gradle getParent();

    /**
     * <p>Returns the root project of this build.</p>
     *
     * @return The root project. Never returns null.
     */
    Project getRootProject();

    /**
     * <p>Returns the {@link TaskExecutionGraph} for this build.</p>
     *
     * @return The task graph. Never returns null.
     */
    TaskExecutionGraph getTaskGraph();

    /**
     * Returns the {@link StartParameter} used to start this build.
     *
     * @return The start parameter. Never returns null.
     */
    StartParameter getStartParameter();

    /**
     * Adds a listener to this build, to receive notifications as projects are evaluated.
     *
     * @param listener The listener to add. Does nothing if this listener has already been added.
     * @return The added listener.
     */
    ProjectEvaluationListener addProjectEvaluationListener(ProjectEvaluationListener listener);

    /**
     * Removes the given listener from this build.
     *
     * @param listener The listener to remove. Does nothing if this listener has not been added.
     */
    void removeProjectEvaluationListener(ProjectEvaluationListener listener);

    /**
     * Adds a closure to be called immediately before a project is evaluated. The project is passed to the closure as a
     * parameter.
     *
     * @param closure The closure to execute.
     */
    void beforeProject(Closure closure);

    /**
     * Adds a closure to be called immediately after a project is evaluated. The project is passed to the closure as the
     * first parameter. The project evaluation failure, if any, is passed as the second parameter. Both parameters are
     * optional.
     *
     * @param closure The closure to execute.
     */
    void afterProject(Closure closure);

    /**
     * Adds a closure to be called when the build is started. This {@code Gradle} instance is passed to the closure as
     * the first parameter.
     *
     * @param closure The closure to execute.
     */
    void buildStarted(Closure closure);

    /**
     * Adds a closure to be called when the build settings have been loaded and evaluated. The settings object is
     * fully configured and is ready to use to load the build projects. The
     * {@link org.gradle.api.initialization.Settings} object is passed to the closure as a parameter.
     *
     * @param closure The closure to execute.
     */
    void settingsEvaluated(Closure closure);

    /**
     * Adds a closure to be called when the projects for the build have been created from the settings.
     * None of the projects have been evaluated. This {@code Gradle} instance is passed to the closure as a parameter.
     *
     * @param closure The closure to execute.
     */
    void projectsLoaded(Closure closure);

    /**
     * Adds a closure to be called when all projects for the build have been evaluated. The project objects are fully
     * configured and are ready to use to populate the task graph. This {@code Gradle} instance is passed to
     * the closure as a parameter.
     *
     * @param closure The closure to execute.
     */
    void projectsEvaluated(Closure closure);

    /**
     * Adds a closure to be called when the build is completed. All selected tasks have been executed.
     * A {@link org.gradle.BuildResult} instance is passed to the closure as a parameter.
     *
     * @param closure The closure to execute.
     */
    void buildFinished(Closure closure);

    /**
     * <p>Adds a {@link BuildListener} to this Build instance. The listener is notified of events which occur during the
     * execution of the build.</p>
     *
     * @param buildListener The listener to add.
     */
    void addBuildListener(BuildListener buildListener);

    /**
     * Adds the given listener to this build. The listener may implement any of the given listener interfaces:
     *
     * <ul>
     *
     * <li>{@link org.gradle.BuildListener}
     *
     * <li>{@link org.gradle.api.execution.TaskExecutionGraphListener}
     *
     * <li>{@link org.gradle.api.ProjectEvaluationListener}
     *
     * <li>{@link org.gradle.api.execution.TaskExecutionListener}
     *
     * <li>{@link org.gradle.api.execution.TaskActionListener}
     *
     * <li>{@link org.gradle.api.logging.StandardOutputListener}
     *
     * <li>{@link org.gradle.api.tasks.testing.TestListener}
     *
     * </ul>
     *
     * @param listener The listener to add. Does nothing if this listener has already been added.
     */
    public void addListener(Object listener);

    /**
     * Removes the given listener from this build.
     *
     * @param listener The listener to remove. Does nothing if this listener has not been added.
     */
    public void removeListener(Object listener);

    /**
     * Uses the given object as a logger. The logger object may implement any of the listener interfaces supported by
     * {@link #addListener(Object)}. Each listener interface has exactly one associated logger. When you call this
     * method with a logger of a given listener type, the new logger will replace whichever logger is currently
     * associated with the listener type. This allows you to selectively replace the standard logging which Gradle
     * provides with your own implementation, for certain types of events.
     *
     * @param logger The logger to use.
     */
    public void useLogger(Object logger);

    /**
     * Returns this {@code Gradle} instance. This method is useful in init scripts to explicitly access Gradle
     * properties and methods. For example, using <code>gradle.parent</code> can express your intent better than using
     * <code>parent</code>. This property also allows you to access Gradle properties from a scope where the property
     * may be hidden, such as, for example, from a method or closure.
     *
     * @return this. Never returns null.
     */
    Gradle getGradle();
}
