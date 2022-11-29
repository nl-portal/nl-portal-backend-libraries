/*
 * Copyright 2015-2022 Ritense BV, the Netherlands.
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
package com.ritense.portal.data.liquibase

import org.springframework.boot.autoconfigure.AutoConfigureAfter
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.boot.autoconfigure.liquibase.LiquibaseProperties
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import javax.sql.DataSource

/**
 * Auto-configuration for Liquibase-runner.
 */
@Configuration
@AutoConfigureAfter(DataSourceAutoConfiguration::class, HibernateJpaAutoConfiguration::class)
@EnableConfigurationProperties(value = [LiquibaseProperties::class])
class LiquibaseRunnerAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean(LiquibaseRunner::class)
    fun liquibaseRunner(
        liquibaseMasterChangeLogLocations: List<LiquibaseMasterChangeLogLocation>,
        liquibaseProperties: LiquibaseProperties,
        datasource: DataSource
    ): LiquibaseRunner {
        return LiquibaseRunner(liquibaseMasterChangeLogLocations, liquibaseProperties, datasource)
    }

    @Bean
    @ConditionalOnMissingBean(LiquibaseInitializationEventListener::class)
    fun liquibaseInitializationEventListener(
        liquibaseRunner: LiquibaseRunner
    ): LiquibaseInitializationEventListener {
        return LiquibaseInitializationEventListener(liquibaseRunner)
    }
}