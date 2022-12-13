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
package com.ritense.portal.erfpachtdossier.service.impl

import com.ritense.portal.commonground.authentication.BedrijfAuthentication
import com.ritense.portal.commonground.authentication.BurgerAuthentication
import com.ritense.portal.commonground.authentication.CommonGroundAuthentication
import com.ritense.portal.erfpachtdossier.client.ErfpachtDossierClient
import com.ritense.portal.erfpachtdossier.domain.Erfpachtdossier
import com.ritense.portal.erfpachtdossier.domain.Erfpachtdossiers
import com.ritense.portal.erfpachtdossier.service.DossierService

class ErfpachtDossierService(
    val erfpachtDossierClient: ErfpachtDossierClient
) : DossierService {
    override suspend fun getDossiers(authentication: CommonGroundAuthentication): Erfpachtdossiers {
        return if (authentication is BurgerAuthentication) {
            erfpachtDossierClient.getDossiers()
        } else if (authentication is BedrijfAuthentication) {
            erfpachtDossierClient.getDossiers()
        } else {
            throw IllegalArgumentException("Cannot get dossiers for this user")
        }
    }

    override suspend fun getDossier(authentication: CommonGroundAuthentication, dossierId: String): Erfpachtdossier {
        return if (authentication is BurgerAuthentication) {
            erfpachtDossierClient.getDossier(dossierId)
        } else if (authentication is BedrijfAuthentication) {
            erfpachtDossierClient.getDossier(dossierId)
        } else {
            throw IllegalArgumentException("Cannot get dossier with id '$dossierId' for this user")
        }
    }
}