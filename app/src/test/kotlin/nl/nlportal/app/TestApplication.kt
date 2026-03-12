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
package nl.nlportal.app

import nl.nlportal.data.liquibase.LiquibaseRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.graphql.GraphQlSourceBuilderCustomizer
import org.springframework.boot.autoconfigure.liquibase.LiquibaseProperties
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder
import reactor.core.publisher.Mono
import java.util.concurrent.CopyOnWriteArraySet
import javax.sql.DataSource

/**
 * Holds the set of GraphQL field names that Spring detected as "unmapped"
 * (i.e. declared in SDL but with no registered data fetcher bean).
 * Populated at startup via inspectSchemaMappings.
 */
class UnmappedGraphQlFields {
    val queryFields: MutableSet<String> = CopyOnWriteArraySet()
    val mutationFields: MutableSet<String> = CopyOnWriteArraySet()
}

/**
 * Test application that uses the REAL OauthSecurityAutoConfiguration.
 * Unlike other module TestApplication classes, this does NOT exclude security.
 * A mock ReactiveJwtDecoder is provided to avoid needing a real Keycloak.
 */
@SpringBootApplication
class TestApplication {
    fun main(args: Array<String>) {
        runApplication<TestApplication>(*args)
    }

    @Bean
    fun unmappedGraphQlFields(): UnmappedGraphQlFields = UnmappedGraphQlFields()

    @Bean
    fun graphQlSourceBuilderCustomizer(unmapped: UnmappedGraphQlFields): GraphQlSourceBuilderCustomizer =
        GraphQlSourceBuilderCustomizer { builder ->
            builder.inspectSchemaMappings { report ->
                report.unmappedFields().forEach { coordinates ->
                    when (coordinates.typeName) {
                        "Query" -> unmapped.queryFields.add(coordinates.fieldName)
                        "Mutation" -> unmapped.mutationFields.add(coordinates.fieldName)
                    }
                }
            }
        }

    /**
     * No-op LiquibaseRunner that prevents LiquibaseRunnerAutoConfiguration from creating its own.
     * In tests, JPA's hbm2ddl.auto=create-drop manages the schema, so Liquibase must not run.
     *
     * LiquibaseRunnerAutoConfiguration uses @ConditionalOnMissingBean(LiquibaseRunner::class),
     * so defining this bean here suppresses the auto-configured LiquibaseRunner.
     *
     * Passing an empty list of change log locations ensures run() iterates over nothing
     * and exits cleanly without attempting to create any tables.
     */
    @Bean
    fun liquibaseRunner(
        liquibaseProperties: LiquibaseProperties,
        datasource: DataSource,
    ): LiquibaseRunner = LiquibaseRunner(emptyList(), liquibaseProperties, datasource)

    @Bean
    fun reactiveJwtDecoder(): ReactiveJwtDecoder {
        // Return a decoder that always fails - we want unauthenticated tests to fail
        return ReactiveJwtDecoder { _ ->
            Mono.error(RuntimeException("No real JWT decoder configured for tests"))
        }
    }
}