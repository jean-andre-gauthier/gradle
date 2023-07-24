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

import org.gradle.integtests.fixtures.DefaultTestExecutionResult
import org.gradle.integtests.fixtures.TargetCoverage
import org.gradle.testing.fixture.AbstractTestingMultiVersionIntegrationTest

import static org.gradle.testing.fixture.JUnitCoverage.JUNIT_JUPITER
import static org.gradle.testing.fixture.JUnitCoverage.getLATEST_JUPITER_VERSION

@TargetCoverage({ JUNIT_JUPITER })
class JUnitJupiterIntermediaryNodesIntegrationTest extends AbstractTestingMultiVersionIntegrationTest implements JUnitJupiterMultiVersionTest {
    def "sets class name on intermediary nodes"() {
        given:
        executer.noExtraLogging()
        file('src/test/java/org/gradle/TestWithIntermediaryNodes.java').text = """
            package org.gradle;

            ${testFrameworkImports}
            import java.util.Arrays;
            import java.util.List;
            import java.util.stream.Stream;
            import org.junit.jupiter.params.ParameterizedTest;
            import org.junit.jupiter.params.provider.Arguments;
            import org.junit.jupiter.params.provider.MethodSource;

            public class TestWithIntermediaryNodes {
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
        """.stripIndent()
        buildScriptWithJupiterDependencies("""
            test {
                useJUnitPlatform()
            }
        """)

        when:
        run('check')

        then:
        def result = new DefaultTestExecutionResult(testDirectory)
        result.assertTestClassesExecuted('org.gradle.TestWithIntermediaryNodes')
    }

    def buildScriptWithJupiterDependencies(script, String version = LATEST_JUPITER_VERSION) {
        buildScript("""
            apply plugin: 'java'

            ${mavenCentralRepository()}
            dependencies {
                testImplementation 'org.junit.jupiter:junit-jupiter:${version}'
                testRuntimeOnly 'org.junit.platform:junit-platform-launcher'
            }
            $script
        """)
    }
}
