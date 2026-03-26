/*
 * Copyright 2015-2023 Ritense BV, the Netherlands.
 *
 * Licensed under EUPL, Version 1.2 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nl.nlportal.app.security

import nl.nlportal.app.TestApplication
import nl.nlportal.core.security.config.SecurityEndpointsConfig
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.TestFactory
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient
import org.springframework.http.HttpMethod
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.util.AntPathMatcher
import org.springframework.web.reactive.result.method.annotation.RequestMappingHandlerMapping

/**
 * Integration test that DYNAMICALLY discovers ALL REST endpoints registered in the
 * Spring WebFlux context and verifies whether they are correctly secured or public.
 *
 * HOW IT WORKS:
 * 1. Injects RequestMappingHandlerMapping to discover all @RestController/@Controller
 *    endpoints registered at runtime (only those active via @ConditionalOnProperty)
 * 2. Injects SecurityEndpointsConfig to read the configured unsecured URL patterns
 *    (nl-portal.security.endpoints.unsecured in application.yml)
 * 3. Classifies each endpoint as PUBLIC or SECURED by matching its path against:
 *    - Hardcoded-public patterns from OauthSecurityAutoConfiguration:
 *        /graphiql, /graphql, /actuator
 *    - Configured unsecured patterns from SecurityEndpointsConfig (e.g. /api/public)
 *    - All other paths are considered SECURED
 * 4. For SECURED endpoints: asserts HTTP 401 is returned without a JWT token
 * 5. For PUBLIC endpoints: asserts HTTP is NOT 401 without a JWT token
 *
 * NEW ENDPOINTS ARE AUTOMATICALLY TESTED - no manual updates needed.
 * When a new @RestController is added, it is automatically discovered and tested.
 *
 * NOTE: Path variables (e.g. {id}) are replaced with a dummy UUID value so the
 * request reaches the security layer (a 401 before routing means security works).
 **/

@SpringBootTest(classes = [TestApplication::class])
@AutoConfigureWebTestClient(timeout = "30000")
@TestInstance(PER_CLASS)
@Tag("integration")
class RestEndpointAuthorizationIT {
    @Autowired
    lateinit var webTestClient: WebTestClient

    @Autowired
    lateinit var requestMappingHandlerMapping: RequestMappingHandlerMapping

    @Autowired
    lateinit var securityEndpointsConfig: SecurityEndpointsConfig

    companion object {
        private val PATH_MATCHER = AntPathMatcher()
        private const val DUMMY_UUID = "00000000-0000-0000-0000-000000000001"

        /**
         * Patterns that are always public per OauthSecurityAutoConfiguration,
         * regardless of SecurityEndpointsConfig. These mirror the hardcoded
         * permitAll() rules in the security filter chain.
         */
        private val ALWAYS_PUBLIC_PATTERNS =
            listOf(
                "/graphiql",
                "/graphql",
                "/actuator/**",
            )
    }

    // ============================================================
    // Dynamic test: every secured endpoint should return 401
    // ============================================================

    @TestFactory
    fun `all secured REST endpoints should require authentication`(): List<DynamicTest> =
        discoverEndpoints()
            .filter { (_, _, isPublic) -> !isPublic }
            .map { (method, path, _) ->
                DynamicTest.dynamicTest("$method $path should require authentication") {
                    webTestClient
                        .method(method)
                        .uri(path)
                        .exchange()
                        .expectStatus()
                        .isUnauthorized
                }
            }

    // ============================================================
    // Dynamic test: every public endpoint should NOT return 401
    // ============================================================

    @TestFactory
    fun `all public REST endpoints should be accessible without authentication`(): List<DynamicTest> =
        discoverEndpoints()
            .filter { (_, _, isPublic) -> isPublic }
            .map { (method, path, _) ->
                DynamicTest.dynamicTest("$method $path should be accessible without authentication") {
                    webTestClient
                        .method(method)
                        .uri(path)
                        .exchange()
                        .expectStatus()
                        .value { status ->
                            assert(status != 401) {
                                """
                                |PUBLIC ENDPOINT BLOCKED: $method $path
                                |returned 401 Unauthorized without a JWT token!
                                |
                                |This endpoint's path matches a public pattern in SecurityEndpointsConfig
                                |or is a known-public path (graphql, actuator), but it is returning 401.
                                |
                                |Check OauthSecurityAutoConfiguration and nl-portal.security.endpoints.unsecured.
                                """.trimMargin()
                            }
                        }
                }
            }

    // ============================================================
    // Endpoint discovery
    // ============================================================

    /**
     * Returns all registered REST endpoint combinations as triples of
     * (HttpMethod, resolvedPath, isPublic).
     *
     * - Path variables like {id} and {documentId} are replaced with a dummy UUID
     *   so requests can be issued without needing real routing knowledge.
     * - Each (method, pattern) combination produces one entry.
     * - Methods with no explicit HTTP method mapping are skipped (e.g. OPTIONS catch-alls).
     */
    private fun discoverEndpoints(): List<Triple<HttpMethod, String, Boolean>> {
        val publicPatterns = ALWAYS_PUBLIC_PATTERNS + securityEndpointsConfig.unsecured

        return requestMappingHandlerMapping.handlerMethods
            .flatMap { (info, _) -> resolveEndpoints(info) }
            .map { (method, path) ->
                Triple(method, path, isPublic(path, publicPatterns))
            }.distinctBy { (method, path, _) -> "$method $path" }
            .sortedWith(compareBy({ it.third }, { it.first.name() }, { it.second }))
    }

    private fun resolveEndpoints(mappingInfo: org.springframework.web.reactive.result.method.RequestMappingInfo): List<Pair<HttpMethod, String>> {
        // In Spring WebFlux, RequestMappingInfo only has getPatternsCondition() (no getPathPatternsCondition).
        // getPatternsCondition() returns a PatternsRequestCondition whose getPatterns() gives Set<PathPattern>.
        // Each PathPattern exposes its string form via patternString.
        val patterns =
            mappingInfo.patternsCondition
                ?.patterns
                ?.map { it.patternString }
                ?.ifEmpty { return emptyList() }
                ?: return emptyList()

        val methods =
            mappingInfo.methodsCondition.methods
                .mapNotNull { runCatching { HttpMethod.valueOf(it.name) }.getOrNull() }
                .ifEmpty { return emptyList() } // skip mappings with no explicit HTTP method

        return patterns.flatMap { pattern ->
            val path = pattern.replace(Regex("\\{[^}]+}"), DUMMY_UUID)
            methods.map { method -> Pair(method, path) }
        }
    }

    private fun isPublic(
        path: String,
        publicPatterns: List<String>,
    ): Boolean = publicPatterns.any { pattern -> PATH_MATCHER.match(pattern, path) }
}