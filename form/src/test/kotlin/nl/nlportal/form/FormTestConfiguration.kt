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
package nl.nlportal.form

import nl.nlportal.core.security.OauthSecurityAutoConfiguration
import org.apache.commons.lang3.StringUtils
import org.springframework.beans.factory.ObjectProvider
import org.springframework.boot.SpringApplication
import org.springframework.boot.SpringBootConfiguration
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.security.SecurityProperties
import org.springframework.context.annotation.Bean
import org.springframework.security.core.userdetails.MapReactiveUserDetailsService
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.crypto.password.PasswordEncoder
import java.util.regex.Pattern

@SpringBootConfiguration
@EnableAutoConfiguration(exclude = [OauthSecurityAutoConfiguration::class])
class FormTestConfiguration {
    fun main(args: Array<String>) {
        SpringApplication.run(FormTestConfiguration::class.java, *args)
    }

    private val NOOP_PASSWORD_PREFIX = "{noop}"

    private val PASSWORD_ALGORITHM_PATTERN: Pattern = Pattern.compile("^\\{.+}.*$")

    @Bean
    @ConditionalOnMissingBean
    fun reactiveUserDetailsService(
        properties: SecurityProperties,
        passwordEncoder: ObjectProvider<PasswordEncoder?>,
    ): MapReactiveUserDetailsService {
        val user: SecurityProperties.User = properties.getUser()
        val userDetails = getUserDetails(user, getOrDeducePassword(user, passwordEncoder.getIfAvailable()))
        return MapReactiveUserDetailsService(userDetails)
    }

    private fun getUserDetails(
        user: SecurityProperties.User,
        password: String,
    ): UserDetails {
        val roles: List<String> = user.getRoles()
        return User.withUsername(user.getName()).password(password).roles(StringUtils.join(roles)).build()
    }

    private fun getOrDeducePassword(
        user: SecurityProperties.User,
        encoder: PasswordEncoder?,
    ): String {
        val password: String = user.getPassword()
        if (user.isPasswordGenerated()) {
        }
        if (encoder != null || PASSWORD_ALGORITHM_PATTERN.matcher(password).matches()) {
            return password
        }
        return NOOP_PASSWORD_PREFIX + password
    }
}