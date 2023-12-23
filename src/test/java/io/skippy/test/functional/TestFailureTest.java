/*
 * Copyright 2023 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.skippy.test.functional;

import io.skippy.test.SkippyVersion;
import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static java.lang.System.lineSeparator;
import static java.nio.file.Files.readString;
import static java.util.regex.Pattern.quote;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

/**
 * Functional test to check that the skippy folder is cleared when skippyAnalyze fails.
 *
 * @author Florian McKee
 */
public class TestFailureTest {

    @Test
    public void testSkippyAnalysisTask() throws Exception {
        var buildFileTemplate = new File(getClass().getResource("test_failure/build.gradle.template").toURI());
        var projectDir = buildFileTemplate.getParentFile();
        String buildFile = readString(buildFileTemplate.toPath()).replaceAll(quote("${skippyVersion}"), SkippyVersion.VERSION);
        Files.writeString(projectDir.toPath().resolve("build.gradle"), buildFile);

        BuildResult result = GradleRunner.create()
                .withProjectDir(projectDir)
                .withArguments("skippyAnalyze", "--refresh-dependencies")
                .forwardOutput()
                .buildAndFail();

        var output = result.getOutput();
        var lines = output.split(lineSeparator());

        assertThat(lines).contains(
            "Clearing skippy folder due to build failure"
        );

        var classesMd5Txt = projectDir.toPath().resolve(Path.of("skippy", "classes.md5"));
        assertThat(classesMd5Txt.toFile().exists()).isFalse();

        var leftPadderTestCov = projectDir.toPath().resolve(Path.of("skippy", "com.example.LeftPadderTest.cov"));
        assertThat(leftPadderTestCov.toFile().exists()).isFalse();

        var rightPadderTestCov = projectDir.toPath().resolve(Path.of("skippy", "com.example.RightPadderTest.cov"));
        assertThat(rightPadderTestCov.toFile().exists()).isFalse();
    }

}