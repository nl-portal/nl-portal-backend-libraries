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

import graphql.schema.GraphQLFieldDefinition
import nl.nlportal.app.TestApplication
import nl.nlportal.app.UnmappedGraphQlFields
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.graphql.test.autoconfigure.tester.AutoConfigureHttpGraphQlTester
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient
import org.springframework.graphql.execution.GraphQlSource
import org.springframework.graphql.test.tester.HttpGraphQlTester

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
    lateinit var httpGraphQlTester: HttpGraphQlTester

    @Autowired
    lateinit var graphQlSource: GraphQlSource

    @Autowired
    lateinit var unmappedGraphQlFields: UnmappedGraphQlFields

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
            )
    }

    // ============================================================
    // Dynamic test: every query should fail without auth
    // ============================================================

    @TestFactory
    fun `all GraphQL queries should require authentication`(): List<DynamicTest> {
        val schema = graphQlSource.schema()
        val queryTypeName = schema.queryType.name

        return schema.queryType.fieldDefinitions
            .filter { it.name !in ALLOWED_PUBLIC_OPERATIONS }
            .filter { field -> field.name !in unmappedGraphQlFields.queryFields }
            .map { field ->
                DynamicTest.dynamicTest("Query '${field.name}' should require authentication") {
                    assertRequiresAuthentication(field, isQuery = true)
                }
            }
    }

    @TestFactory
    fun `all GraphQL mutations should require authentication`(): List<DynamicTest> {
        val schema = graphQlSource.schema()
        val mutationType = schema.mutationType ?: return emptyList()

        return mutationType.fieldDefinitions
            .filter { it.name !in ALLOWED_PUBLIC_OPERATIONS }
            .filter { field -> field.name !in unmappedGraphQlFields.mutationFields }
            .map { field ->
                DynamicTest.dynamicTest("Mutation '${field.name}' should require authentication") {
                    assertRequiresAuthentication(field, isQuery = false)
                }
            }
    }

    // ============================================================
    // Dynamic test: every public query should succeed without auth
    // ============================================================

    @TestFactory
    fun `all public GraphQL queries should be accessible without authentication`(): List<DynamicTest> {
        val schema = graphQlSource.schema()

        return schema.queryType.fieldDefinitions
            .filter { it.name in ALLOWED_PUBLIC_OPERATIONS }
            .map { field ->
                DynamicTest.dynamicTest("Query '${field.name}' should be accessible without authentication") {
                    assertAccessibleWithoutAuthentication(field, isQuery = true)
                }
            }
    }

    @TestFactory
    fun `all public GraphQL mutations should be accessible without authentication`(): List<DynamicTest> {
        val schema = graphQlSource.schema()
        val mutationType = schema.mutationType ?: return emptyList()

        return mutationType.fieldDefinitions
            .filter { it.name in ALLOWED_PUBLIC_OPERATIONS }
            .map { field ->
                DynamicTest.dynamicTest("Mutation '${field.name}' should be accessible without authentication") {
                    assertAccessibleWithoutAuthentication(field, isQuery = false)
                }
            }
    }

    // ============================================================
    // Sanity check: verify schema has expected number of endpoints
    // ============================================================

    @Test
    fun `schema should have queries registered`() {
        val schema = graphQlSource.schema()
        val totalCount = schema.queryType.fieldDefinitions.size
        val activeCount =
            schema.queryType.fieldDefinitions
                .count { field -> field.name !in unmappedGraphQlFields.queryFields }
        assert(totalCount > 0) {
            "No queries found in GraphQL schema SDL - something is wrong with schema assembly"
        }
        println("Discovered $activeCount active GraphQL queries (of $totalCount declared in SDL)")
    }

    @Test
    fun `schema should have mutations registered`() {
        val schema = graphQlSource.schema()
        val mutationType = schema.mutationType
        val totalCount = mutationType?.fieldDefinitions?.size ?: 0
        val activeCount =
            mutationType
                ?.fieldDefinitions
                ?.count { field -> field.name !in unmappedGraphQlFields.mutationFields } ?: 0
        assert(totalCount > 0) {
            "No mutations found in GraphQL schema SDL - something is wrong with schema assembly"
        }
        println("Discovered $activeCount active GraphQL mutations (of $totalCount declared in SDL)")
    }

    // ============================================================
    // Helper: execute operation and verify auth is required
    // ============================================================

    private fun assertAccessibleWithoutAuthentication(
        field: GraphQLFieldDefinition,
        isQuery: Boolean,
    ) {
        val document =
            if (isQuery) {
                GraphQLQueryBuilder.buildQuery(field)
            } else {
                GraphQLQueryBuilder.buildMutation(field)
            }

        httpGraphQlTester
            .document(document)
            .execute()
            .errors()
            .satisfy { errors ->
                val authErrors =
                    errors.filter { error ->
                        error.extensions["classification"] == "UNAUTHORIZED" ||
                            error.message?.contains("Unauthorized", ignoreCase = true) == true ||
                            error.message?.contains("Access Denied", ignoreCase = true) == true ||
                            error.message?.contains("403", ignoreCase = true) == true
                    }
                assert(authErrors.isEmpty()) {
                    """
                    |PUBLIC ENDPOINT BLOCKED: ${if (isQuery) "Query" else "Mutation"} '${field.name}'
                    |returned an authentication error when called without authentication!
                    |
                    |This endpoint is listed in ALLOWED_PUBLIC_OPERATIONS but is not actually public.
                    |Either remove it from ALLOWED_PUBLIC_OPERATIONS, or remove the
                    |'authentication: CommonGroundAuthentication' parameter from the resolver method.
                    |
                    |Auth errors: ${authErrors.joinToString { it.message ?: "unknown" }}
                    |Query used: $document
                    """.trimMargin()
                }
            }
    }

    private fun assertRequiresAuthentication(
        field: GraphQLFieldDefinition,
        isQuery: Boolean,
    ) {
        val document =
            if (isQuery) {
                GraphQLQueryBuilder.buildQuery(field)
            } else {
                GraphQLQueryBuilder.buildMutation(field)
            }

        httpGraphQlTester
            .document(document)
            .execute()
            .errors()
            .satisfy { errors ->
                assert(errors.isNotEmpty()) {
                    """
                    |SECURITY VIOLATION: ${if (isQuery) "Query" else "Mutation"} '${field.name}'
                    |returned no errors when called without authentication!
                    |
                    |This means the endpoint is publicly accessible.
                    |
                    |If this is INTENTIONAL, add '${field.name}' to
                    |ALLOWED_PUBLIC_OPERATIONS in GraphQLEndpointAuthorizationIT.
                    |
                    |If this is a BUG, add 'authentication: CommonGroundAuthentication'
                    |as a parameter to the resolver method.
                    |
                    |Query used: $document
                    """.trimMargin()
                }
            }
    }
}