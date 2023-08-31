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

object ApiDependencies {
    val commonsIo by lazy { "commons-io:commons-io:${ApiVersions.commonsIo}" }
    val apacheCommons by lazy { "org.apache.commons:commons-lang3:${ApiVersions.apacheCommons}"}
    val formFlow by lazy { "com.ritense.valtimo:form-flow:${ApiVersions.formFlow}"}
    val graphqlJava by lazy { "com.graphql-java:graphql-java:${ApiVersions.graphqlJava}"}
    val graphqlJavaExtendedScalars by lazy { "com.graphql-java:graphql-java-extended-scalars:${ApiVersions.graphqlJava}"}
    val graphqlKotlinHooksProvider by lazy { "com.expediagroup:graphql-kotlin-hooks-provider:${ApiVersions.graphqlKotlin}"}
    val graphqlKotlinSpringServer by lazy { "com.expediagroup:graphql-kotlin-spring-server:${ApiVersions.graphqlKotlin}"}
    val hibernateTypes by lazy { "com.vladmihalcea:hibernate-types-60:${ApiVersions.hibernateTypes}"}
    val kotlinLogging by lazy { "io.github.microutils:kotlin-logging:${ApiVersions.kotlinLogging}"}
    val springCloudStream by lazy { "org.springframework.cloud:spring-cloud-stream:${ApiVersions.springCloud}" }
    val springCloudStreamBinderRabbit by lazy { "org.springframework.cloud:spring-cloud-stream-binder-rabbit:${ApiVersions.springCloud}" }
}