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

package org.gradle.initialization;

import org.gradle.CacheUsage;
import org.gradle.CommandLineArgumentException;
import org.gradle.StartParameter;
import org.gradle.api.internal.artifacts.ProjectDependenciesBuildInstruction;
import org.gradle.api.logging.LogLevel;
import org.gradle.groovy.scripts.UriScriptSource;
import org.gradle.util.GUtil;
import org.gradle.util.TemporaryFolder;
import org.gradle.util.TestFile;
import org.gradle.util.WrapUtil;
import org.junit.Rule;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static org.gradle.util.WrapUtil.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

/**
 * @author Hans Dockter
 */
public class DefaultCommandLineConverterTest {
    @Rule
    public TemporaryFolder testDir = new TemporaryFolder();

    private TestFile currentDir = testDir.file("current-dir");
    private File expectedBuildFile;
    private File expectedGradleUserHome = StartParameter.DEFAULT_GRADLE_USER_HOME;
    private File expectedProjectDir = currentDir;
    private List<String> expectedTaskNames = toList();
    private Set<String> expectedExcludedTasks = toSet();
    private ProjectDependenciesBuildInstruction expectedProjectDependenciesBuildInstruction
            = new ProjectDependenciesBuildInstruction(WrapUtil.<String>toList());
    private Map<String, String> expectedSystemProperties = new HashMap<String, String>();
    private Map<String, String> expectedProjectProperties = new HashMap<String, String>();
    private List<File> expectedInitScripts = new ArrayList<File>();
    private CacheUsage expectedCacheUsage = CacheUsage.ON;
    private boolean expectedSearchUpwards = true;
    private boolean expectedDryRun;
    private StartParameter.ShowStacktrace expectedShowStackTrace = StartParameter.ShowStacktrace.INTERNAL_EXCEPTIONS;
    private String expectedEmbeddedScript = "somescript";
    private LogLevel expectedLogLevel = LogLevel.LIFECYCLE;
    private boolean expectedColorOutput = true;
    private StartParameter actualStartParameter;
    private boolean expectedProfile;

    private final DefaultCommandLineConverter commandLineConverter = new DefaultCommandLineConverter();

    @Test
    public void withoutAnyOptions() throws IOException {
        checkConversion();
    }

    private void checkConversion(String... args) throws IOException {
        checkConversion(false, args);
    }

    private void checkStartParameter(StartParameter startParameter) throws IOException {
        assertEquals(expectedBuildFile, startParameter.getBuildFile());
        assertEquals(expectedTaskNames, startParameter.getTaskNames());
        assertEquals(expectedProjectDependenciesBuildInstruction,
                startParameter.getProjectDependenciesBuildInstruction());
        assertEquals(expectedProjectDir.getCanonicalPath(), startParameter.getCurrentDir().getCanonicalPath());
        assertEquals(expectedCacheUsage, startParameter.getCacheUsage());
        assertEquals(expectedSearchUpwards, startParameter.isSearchUpwards());
        assertEquals(expectedProjectProperties, startParameter.getProjectProperties());
        assertEquals(expectedSystemProperties, startParameter.getSystemPropertiesArgs());
        assertEquals(expectedGradleUserHome.getCanonicalPath(), startParameter.getGradleUserHomeDir().getCanonicalPath());
        assertEquals(expectedLogLevel, startParameter.getLogLevel());
        assertEquals(expectedColorOutput, startParameter.isColorOutput());
        assertEquals(expectedDryRun, startParameter.isDryRun());
        assertEquals(expectedShowStackTrace, startParameter.getShowStacktrace());
        assertEquals(expectedExcludedTasks, startParameter.getExcludedTaskNames());
        assertEquals(expectedInitScripts, startParameter.getInitScripts());
        assertEquals(expectedProfile, startParameter.isProfile());
    }

    private void checkConversion(final boolean embedded, String... args) throws IOException {
        actualStartParameter = new StartParameter();
        actualStartParameter.setCurrentDir(currentDir);
        commandLineConverter.convert(Arrays.asList(args), actualStartParameter);
        // We check the params passed to the build factory
        checkStartParameter(actualStartParameter);
        if (embedded) {
            assertThat(actualStartParameter.getBuildScriptSource().getResource().getText(), equalTo(expectedEmbeddedScript));
        } else {
            assert !GUtil.isTrue(actualStartParameter.getBuildScriptSource());
        }
    }

    @Test
    public void withSpecifiedGradleUserHomeDirectory() throws IOException {
        expectedGradleUserHome = testDir.file("home");
        checkConversion("-g", expectedGradleUserHome.getCanonicalPath());

        expectedGradleUserHome = currentDir.file("home");
        checkConversion("-g", "home");
    }

    @Test
    public void withSpecifiedProjectDirectory() throws IOException {
        expectedProjectDir = testDir.file("project-dir");
        checkConversion("-p", expectedProjectDir.getCanonicalPath());

        expectedProjectDir = currentDir.file("project-dir");
        checkConversion("-p", "project-dir");
    }

    @Test
    public void withSpecifiedBuildFileName() throws IOException {
        expectedBuildFile = testDir.file("somename");
        expectedProjectDir = expectedBuildFile.getParentFile();
        checkConversion("-b", expectedBuildFile.getCanonicalPath());

        expectedBuildFile = currentDir.file("somename");
        expectedProjectDir = expectedBuildFile.getParentFile();
        checkConversion("-b", "somename");
    }

    @Test
    public void withSpecifiedSettingsFileName() throws IOException {
        File expectedSettingsFile = currentDir.file("somesettings");
        expectedProjectDir = expectedSettingsFile.getParentFile();

        checkConversion("-c", "somesettings");

        assertThat(actualStartParameter.getSettingsScriptSource(), instanceOf(UriScriptSource.class));
        assertThat(actualStartParameter.getSettingsScriptSource().getResource().getFile(), equalTo(expectedSettingsFile));
    }

    @Test
    public void withInitScripts() throws IOException {
        File script1 = currentDir.file("init1.gradle");
        expectedInitScripts.add(script1);
        checkConversion("-Iinit1.gradle");

        File script2 = currentDir.file("init2.gradle");
        expectedInitScripts.add(script2);
        checkConversion("-Iinit1.gradle", "-Iinit2.gradle");
    }
    
    @Test
    public void withSystemProperties() throws IOException {
        final String prop1 = "gradle.prop1";
        final String valueProp1 = "value1";
        final String prop2 = "gradle.prop2";
        final String valueProp2 = "value2";
        expectedSystemProperties = toMap(prop1, valueProp1);
        expectedSystemProperties.put(prop2, valueProp2);
        checkConversion("-D", prop1 + "=" + valueProp1, "-D", prop2 + "=" + valueProp2);
    }

    @Test
    public void withStartProperties() throws IOException {
        final String prop1 = "prop1";
        final String valueProp1 = "value1";
        final String prop2 = "prop2";
        final String valueProp2 = "value2";
        expectedProjectProperties = toMap(prop1, valueProp1);
        expectedProjectProperties.put(prop2, valueProp2);
        checkConversion("-P", prop1 + "=" + valueProp1, "-P", prop2 + "=" + valueProp2);
    }

    @Test
    public void withTaskNames() throws IOException {
        expectedTaskNames = toList("a", "b");
        checkConversion("a", "b");
    }

    @Test
    public void withRebuildCacheFlagSet() throws IOException {
        expectedCacheUsage = CacheUsage.REBUILD;
        checkConversion("-C", "rebuild");
    }

    @Test
    public void withCacheOnFlagSet() throws IOException {
        checkConversion("-C", "on");
    }

    @Test(expected = CommandLineArgumentException.class)
    public void withUnknownCacheFlags() throws IOException {
        checkConversion("-C", "unknown");
    }

    @Test
    public void withSearchUpwardsFlagSet() throws IOException {
        expectedSearchUpwards = false;
        checkConversion("-u");
    }

    @Test
    public void withShowFullStacktrace() throws IOException {
        expectedShowStackTrace = StartParameter.ShowStacktrace.ALWAYS_FULL;
        checkConversion("-S");
    }

    @Test
    public void withShowStacktrace() throws IOException {
        expectedShowStackTrace = StartParameter.ShowStacktrace.ALWAYS;
        checkConversion("-s");
    }

    @Test(expected = CommandLineArgumentException.class)
    public void withShowStacktraceAndShowFullStacktraceShouldThrowCommandLineArgumentEx() throws IOException {
        checkConversion("-sf");
    }

    @Test
    public void withDryRunFlagSet() throws IOException {
        expectedDryRun = true;
        checkConversion("-m");
    }

    @Test
    public void withExcludeTask() throws IOException {
        expectedExcludedTasks.add("excluded");
        checkConversion("-x", "excluded");
        expectedExcludedTasks.add("excluded2");
        checkConversion("-x", "excluded", "-x", "excluded2");
    }

    @Test
    public void withEmbeddedScript() throws IOException {
        expectedSearchUpwards = false;
        checkConversion(true, "-e", expectedEmbeddedScript);
    }

    @Test(expected = CommandLineArgumentException.class)
    public void withEmbeddedScriptAndConflictingNoSearchUpwardsOption() throws IOException {
        checkConversion("-e", "someScript", "-u", "clean");
    }

    @Test(expected = CommandLineArgumentException.class)
    public void withEmbeddedScriptAndConflictingSpecifyBuildFileOption() throws IOException {
        checkConversion("-e", "someScript", "-bsomeFile", "clean");
    }

    @Test(expected = CommandLineArgumentException.class)
    public void withEmbeddedScriptAndConflictingSpecifySettingsFileOption() throws IOException {
        checkConversion("-e", "someScript", "-csomeFile", "clean");
    }

    @Test
    public void withNoProjectDependencyRebuild() throws IOException {
        expectedProjectDependenciesBuildInstruction = new ProjectDependenciesBuildInstruction(null);
        checkConversion("-a");
    }

    @Test
    public void withProjectDependencyTaskNames() throws IOException {
        expectedProjectDependenciesBuildInstruction = new ProjectDependenciesBuildInstruction(WrapUtil.toList("task1",
                "task2"));
        checkConversion("-Atask1", "-A", "task2");
    }

    @Test
    public void withQuietLoggingOptions() throws IOException {
        expectedLogLevel = LogLevel.QUIET;
        checkConversion("-q");
    }

    @Test
    public void withInfoLoggingOptions() throws IOException {
        expectedLogLevel = LogLevel.INFO;
        checkConversion("-i");
    }

    @Test
    public void withDebugLoggingOptions() throws IOException {
        expectedLogLevel = LogLevel.DEBUG;
        checkConversion("-d");
    }

    @Test
    public void withNoColor() throws IOException {
        expectedColorOutput = false;
        checkConversion("--no-color");
    }

    @Test
    public void withShowTasks() throws IOException {
        expectedTaskNames = toList("tasks");
        checkConversion(false, "-t");
    }

    @Test
    public void withShowAllTasks() throws IOException {
        expectedTaskNames = toList("tasks", "--all");
        checkConversion(false, "-t", "--all");
    }

    @Test
    public void withShowTasksAndEmbeddedScript() throws IOException {
        expectedSearchUpwards = false;
        expectedTaskNames = toList("tasks");
        checkConversion(true, "-e", expectedEmbeddedScript, "-t");
    }

    @Test
    public void withShowProperties() throws IOException {
        expectedTaskNames = toList("properties");
        checkConversion(false, "-r");
    }

    @Test
    public void withShowDependencies() throws IOException {
        expectedTaskNames = toList("dependencies");
        checkConversion(false, "-n");
    }

    @Test(expected = CommandLineArgumentException.class)
    public void withLowerPParameterWithoutArgument() throws IOException {
        checkConversion("-p");
    }

    @Test(expected = CommandLineArgumentException.class)
    public void withAParameterWithoutArgument() throws IOException {
        checkConversion("-A");
    }

    @Test(expected = CommandLineArgumentException.class)
    public void withUpperAAndLowerAParameter() throws IOException {
        checkConversion("-a", "-Atask1");
    }

    @Test
    public void withProfile() throws IOException {
        expectedProfile = true;
        checkConversion("--profile");
    }

    @Test(expected = CommandLineArgumentException.class)
    public void withUnknownOption() throws IOException {
        checkConversion("--unknown");
    }

    @Test
    public void withTaskAndTaskOption() throws IOException {
        expectedTaskNames = toList("someTask", "--some-task-option");
        checkConversion("someTask", "--some-task-option");
    }

}
