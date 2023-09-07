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
package nl.nlportal.klant.contactmomenten.graphql

import nl.nlportal.klant.contactmomenten.domain.ContactMoment
import nl.nlportal.klant.generiek.domain.ResultPage

class ContactMomentPage(
    number: Int,
    content: List<ContactMoment>,
    totalElements: Int,
) : Page<ContactMoment>(number, content, totalElements) {

    companion object {
        fun fromResultPage(pageNumber: Int, resultPage: ResultPage<ContactMoment>): ContactMomentPage {
            return ContactMomentPage(
                number = pageNumber,
                content = resultPage.results,
                totalElements = resultPage.count
            )
        }
    }
}