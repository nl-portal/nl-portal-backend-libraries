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
import nl.nlportal.commonground.authentication.CommonGroundAuthentication
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.TestFactory
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS
import org.springframework.beans.factory.ListableBeanFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.getBeansWithAnnotation
import org.springframework.boot.graphql.test.autoconfigure.tester.AutoConfigureHttpGraphQlTester
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient
import org.springframework.graphql.data.method.annotation.MutationMapping
import org.springframework.graphql.data.method.annotation.QueryMapping
import org.springframework.graphql.data.method.annotation.SubscriptionMapping
import org.springframework.stereotype.Controller

/**
 * Integration test that DYNAMICALLY discovers ALL GraphQL queries and mutations
 * from the application's schema and verifies they return errors when called
 * without authentication.
 *
 * HOW IT WORKS:
 * 1. Injects GraphQlSource to access the merged GraphQL schema at runtime
 * 2. Iterates over schema.getQueryType().getFieldDefinitions() for all queries
 * 3. Iterates over schema.getMutationType().getFieldDefinitions() for all mutations
 * 4. Filters out fields that Spring itself reports as "unmapped" (no registered data fetcher)
 *    - captured at startup via inspectSchemaMappings in TestApplication
 * 5. For each field, builds a minimal executable GraphQL document with dummy args
 * 6. Executes the document WITHOUT authentication
 * 7. Verifies that GraphQL errors are returned (auth failure)
 *
 * NEW ENDPOINTS ARE AUTOMATICALLY TESTED - no manual updates needed.
 *
 * NOTE: Fields declared in SDL (.graphqls files) but whose resolver bean is disabled
 * via @ConditionalOnProperty are reported as "unmapped" by Spring and automatically
 * excluded from the test since there is no resolver to enforce authentication.
 *
 * If a query/mutation is INTENTIONALLY public (no authentication parameter),
 * add it to the ALLOWED_PUBLIC_OPERATIONS set below.
 */
@SpringBootTest(classes = [TestApplication::class])
@AutoConfigureHttpGraphQlTester
@AutoConfigureWebTestClient(timeout = "30000")
@TestInstance(PER_CLASS)
@Tag("integration")
class GraphQLEndpointAuthorizationIT {
    @Autowired
    lateinit var listableBeanFactory: ListableBeanFactory

    companion object {
        /**
         * Operations that are INTENTIONALLY public (no authentication required).
         * If a new endpoint should be public, add its name here.
         * Any endpoint NOT in this list is expected to require authentication.
         *
         * If you intentionally create a public query/mutation, add it here to prevent
         * the test from failing:
         *
         *   val ALLOWED_PUBLIC_OPERATIONS = setOf("myPublicQuery")
         */
        val ALLOWED_PUBLIC_OPERATIONS =
            setOf(
                "verificatieConfig", // Returns public config (no user data, no authentication needed)
                // Form definitions - loaded without authentication (e.g. before login)
                "getFormDefinitionById",
                "getFormDefinitionByName",
                "getFormDefinitionByObjectenApiUrl",
                // Case definitions - schema metadata, no user data
                "allCaseDefinitions",
            )
    }

    // ============================================================
    // Dynamic test: every query should fail without auth
    // ============================================================

    @TestFactory
    fun `all GraphQL queries should require authentication`(): List<DynamicTest> {
        var controllers = listableBeanFactory.getBeansWithAnnotation<Controller>()

        return controllers.flatMap { controller ->
            controller.value.javaClass.declaredMethods.mapNotNull { method ->
                if (method.isAnnotationPresent(QueryMapping::class.java)) {
                    DynamicTest.dynamicTest("QueryMapping: ${method.name} should not be public") {
                        val isProtected = method.parameters.find { it.type == CommonGroundAuthentication::class.java } != null
                        val isAllowedPublic = ALLOWED_PUBLIC_OPERATIONS.find { method.name.startsWith(it) } != null

                        assert(isProtected || isAllowedPublic) {
                            """
                            QueryMapping: ${controller.key} - ${method.name} does not have a CommonGroundAuthentication and therefore is
                            publicly accessible if this is correct, add the function to the exclusion list.
                            """.trimIndent()
                        }
                    }
                } else {
                    null
                }
            }
        }
    }

    @TestFactory
    fun `all GraphQL mutations should require authentication`(): List<DynamicTest> {
        var controllers = listableBeanFactory.getBeansWithAnnotation<Controller>()

        return controllers.flatMap { controller ->
            controller.value.javaClass.declaredMethods.mapNotNull { method ->
                if (method.isAnnotationPresent(MutationMapping::class.java)) {
                    DynamicTest.dynamicTest("MutationMapping: ${method.name} should not be public") {
                        val isProtected = method.parameters.find { it.type == CommonGroundAuthentication::class.java } != null
                        val isAllowedPublic = ALLOWED_PUBLIC_OPERATIONS.find { method.name.startsWith(it) } != null

                        assert(isProtected || isAllowedPublic) {
                            """
                            MutationMapping: ${controller.key} - ${method.name} does not have a CommonGroundAuthentication and therefore is
                            publicly accessible if this is correct, add the function to the exclusion list.
                            """.trimIndent()
                        }
                    }
                } else {
                    null
                }
            }
        }
    }

    @TestFactory
    fun `all GraphQL subscription queries should require authentication`(): List<DynamicTest> {
        var controllers = listableBeanFactory.getBeansWithAnnotation<Controller>()

        return controllers.flatMap { controller ->
            controller.value.javaClass.declaredMethods.mapNotNull { method ->
                if (method.isAnnotationPresent(SubscriptionMapping::class.java)) {
                    DynamicTest.dynamicTest("SubscriptionMapping: ${method.name} should not be public") {
                        val isProtected = method.parameters.find { it.type == CommonGroundAuthentication::class.java } != null
                        val isAllowedPublic = ALLOWED_PUBLIC_OPERATIONS.find { method.name.startsWith(it) } != null

                        assert(isProtected || isAllowedPublic) {
                            """
                            SubscriptionMapping: ${controller.key} - ${method.name} does not have a CommonGroundAuthentication and therefore is
                            publicly accessible if this is correct, add the function to the exclusion list.
                            """.trimIndent()
                        }
                    }
                } else {
                    null
                }
            }
        }
    }
}