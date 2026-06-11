/*
 * Copyright 2025 Ritense BV, the Netherlands.
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
package nl.nlportal.app.architecture

import com.tngtech.archunit.base.DescribedPredicate
import com.tngtech.archunit.core.domain.JavaClasses
import com.tngtech.archunit.core.domain.JavaMethod
import com.tngtech.archunit.core.domain.JavaModifier
import com.tngtech.archunit.core.domain.properties.CanBeAnnotated.Predicates.annotatedWith
import com.tngtech.archunit.core.importer.ClassFileImporter
import com.tngtech.archunit.lang.ArchCondition
import com.tngtech.archunit.lang.ConditionEvents
import com.tngtech.archunit.lang.SimpleConditionEvent
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods
import nl.nlportal.commonground.authentication.CommonGroundAuthentication
import org.junit.jupiter.api.Test
import org.springframework.graphql.data.method.annotation.MutationMapping
import org.springframework.graphql.data.method.annotation.QueryMapping
import org.springframework.stereotype.Controller

class GraphQlOperationAuthenticationArchTest {
    private val importedClasses: JavaClasses = ClassFileImporter().importPackages(BASE_PACKAGE)

    // Definition queries (e.g. FormDefinitionQuery, CaseDefinitionQuery) only return schema/definition
    // metadata, not citizen data, so they are intentionally unauthenticated.
    @Test
    fun `graphql query and mutation methods must declare a CommonGroundAuthentication parameter`() {
        methods()
            .that(annotatedWithQueryOrMutationMapping())
            .and().doNotHaveModifier(JavaModifier.SYNTHETIC)
            .and().areDeclaredInClassesThat().areAnnotatedWith(Controller::class.java)
            .and().areDeclaredInClassesThat().resideInAPackage("..graphql..")
            .and().areDeclaredInClassesThat().haveSimpleNameNotEndingWith("DefinitionQuery")
            .should(declareCommonGroundAuthenticationParameter())
            .check(importedClasses)
    }

    private fun annotatedWithQueryOrMutationMapping(): DescribedPredicate<JavaMethod> =
        annotatedWith(QueryMapping::class.java)
            .or(annotatedWith(MutationMapping::class.java))
            .forSubtype()

    private fun declareCommonGroundAuthenticationParameter(): ArchCondition<JavaMethod> =
        object : ArchCondition<JavaMethod>("declare a parameter of type $AUTHENTICATION_TYPE") {
            override fun check(method: JavaMethod, events: ConditionEvents) {
                val hasAuthentication = method.rawParameterTypes.any { it.fullName == AUTHENTICATION_TYPE }
                if (!hasAuthentication) {
                    events.add(
                        SimpleConditionEvent.violated(
                            method,
                            "${method.fullName} does not declare a $AUTHENTICATION_TYPE parameter",
                        ),
                    )
                }
            }
        }

    companion object {
        private const val BASE_PACKAGE = "nl.nlportal"
        private val AUTHENTICATION_TYPE = CommonGroundAuthentication::class.java.name
    }
}
