/*
 * Copyright 2023 the original author or authors.
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

package org.gradle.testing.junit.jupiter


import org.gradle.integtests.fixtures.HtmlTestExecutionResult
import org.gradle.integtests.fixtures.TargetCoverage
import org.gradle.testing.fixture.AbstractTestingMultiVersionIntegrationTest

import static org.gradle.testing.fixture.JUnitCoverage.JUNIT_JUPITER
import static org.hamcrest.CoreMatchers.containsString

@TargetCoverage({ JUNIT_JUPITER })
class JUnitJupiterIntermediaryNodesIntegrationTest extends AbstractTestingMultiVersionIntegrationTest implements JUnitJupiterMultiVersionTest {

    def "sets class name on intermediary nodes"() {
        given:
        buildScript """
            $junitSetup
            dependencies {
                testImplementation(platform('org.junit:junit-bom:$version'))
                testImplementation('org.junit.platform:junit-platform-launcher')
            }
            test {
                beforeSuite {
                    println("beforeSuite: " + it.className + " " + it.name)
                }

                beforeTest {
                    println("beforeTest: " + it.className + " " + it.name)
                }
            }
        """

        and:
        testClass "SomeTest"

        when:
        succeeds "test"

        then:
        new HtmlTestExecutionResult(testDirectory)
            .testClassStartsWith("SomeTest")
            // This assertion fails, because the class name is not set on intermediary nodes (there's a similar issue with JUnit 4 too)
            .assertStdout(containsString("beforeSuite: SomeTest test(int, int)"))
    }

    private String getJunitSetup() {
        """
            apply plugin: 'java'
            ${mavenCentralRepository()}
            dependencies {
                ${testFrameworkDependencies}
                testImplementation 'org.junit.jupiter:junit-jupiter:${version}'
            }
            test.${configureTestFramework}
        """.stripIndent()
    }

    private void testClass(String name) {
        file("src/test/java/${name}.java") << """
            ${testFrameworkImports}
            import java.util.Arrays;
            import java.util.List;
            import java.util.stream.Stream;
            import org.junit.jupiter.params.ParameterizedTest;
            import org.junit.jupiter.params.provider.Arguments;
            import org.junit.jupiter.params.provider.MethodSource;
            public class $name {
                @ParameterizedTest
                @MethodSource("data")
                void test(int a, int b) {
                    assert true;
                }

                static Stream<Arguments> data() {
                    System.out.println("> intermediary");
                    return Stream.of(
                        Arguments.of(0, 0),
                        Arguments.of(1, 1)
                    );
                }
            }
        """
    }
}
