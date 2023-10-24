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
package com.ritense.portal.data.liquibase

import liquibase.Contexts
import liquibase.Liquibase
import liquibase.database.Database
import liquibase.database.DatabaseFactory
import liquibase.database.jvm.JdbcConnection
import liquibase.exception.DatabaseException
import liquibase.exception.LiquibaseException
import liquibase.resource.ClassLoaderResourceAccessor
import mu.KotlinLogging
import org.springframework.boot.autoconfigure.liquibase.LiquibaseProperties
import java.sql.SQLException
import javax.sql.DataSource

class LiquibaseRunner(
    private val liquibaseMasterChangeLogLocations: List<LiquibaseMasterChangeLogLocation>,
    liquibaseProperties: LiquibaseProperties,
    private val datasource: DataSource
) {
    private val context: Contexts

    fun run() {
        val connection = datasource.connection
        val jdbcConnection = JdbcConnection(connection)
        val database: Database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(jdbcConnection)
        try {
            for (changeLogLocation in liquibaseMasterChangeLogLocations) {
                runChangeLog(database, changeLogLocation.filePath)
            }
        } catch (e: LiquibaseException) {
            throw DatabaseException(e)
        } finally {
            if (connection != null) {
                try {
                    connection.rollback()
                    connection.close()
                } catch (e: SQLException) {
                    logger.error("Error closing connection ", e)
                }
            }
        }
        logger.info("Finished running liquibase")
    }

    private fun runChangeLog(database: Database, filePath: String?) {
        val liquibase = Liquibase(filePath, ClassLoaderResourceAccessor(), database)
        logger.info("Running liquibase master changelog: {}", liquibase.changeLogFile)
        liquibase.update(context)
    }

    init {
        context = Contexts(liquibaseProperties.contexts)
    }

    companion object {
        private val logger = KotlinLogging.logger {}
    }
}