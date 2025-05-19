/*
 * Copyright 2015-2021 Ritense BV, the Netherlands.
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

/* add thema */

INSERT INTO public.producttypen_thema(id, uuid, gepubliceerd, aanmaak_datum, update_datum, naam, beschrijving, hoofd_thema_id)
VALUES (1, '40e15b22-e5d1-4d91-ade7-d52a894351e1', true, now(), now(), 'HoofdThema', 'Hoofd thema', null);

INSERT INTO public.producttypen_thema(id, uuid, gepubliceerd, aanmaak_datum, update_datum, naam, beschrijving, hoofd_thema_id)
VALUES (2, 'f5a67141-a3af-40f9-83c5-c98275d30858', true, now(), now(), 'SubThema', 'Sub thema', 1);

INSERT INTO public.producttypen_thema(id, uuid, gepubliceerd, aanmaak_datum, update_datum, naam, beschrijving, hoofd_thema_id)
VALUES (3, '41f71c2e-9e0c-4a1b-8d39-709669b256c2', true, now(), now(), 'Parkeren', 'Parkeren thema', 2);

INSERT INTO public.producttypen_thema(id, uuid, gepubliceerd, aanmaak_datum, update_datum, naam, beschrijving, hoofd_thema_id)
VALUES (4, 'c810912d-6d3c-42c2-8021-aeca49d9b89b', true, now(), now(), 'Belastingzaken', 'Belastingzaken thema', null);

/* add schema */
INSERT INTO public.producttypen_jsonschema(id, naam, schema)
VALUES (1, 'parkeervergunning-verbruiksobject', '{
  "type": "object",
  "title": "PDCverbruiksObject",
  "$schema": "http://json-schema.org/draft-07/schema",
  "examples": [
    {
      "data": {
        "mijnExtraData": "abc",
        "Een genest object": {
          "nogMeer": "data"
        }
      },
      "soort": "gemeldeVerhuur",
      "productInstantie": "7d9cd6c2-8147-46f2-9ae9-c67e8213c116"
    }
  ],
  "required": [
    "soort",
    "productInstantie",
    "data"
  ],
  "properties": {
    "data": {
      "type": "object",
      "description": "Vrij veld voor verbruiksobject data"
    },
    "soort": {
      "type": "string",
      "description": "Soort / type verbruiksobject. Alle verbruiksobjecten van het zelfde soort hebben hetzelfde type"
    },
    "productInstantie": {
      "type": "string",
      "format": "uuid",
      "description": "UUID van bijhorend PDCProductInstantie"
    }
  },
  "description": "Product verbruik details van een instantie uit het PDC. Bevat product data die vaak aangepast moet worden. Bijvoorbeeld een log van gebruikte tijdsvakken",
  "additionalProperties": false
}'),
(2, 'parkeervergunning-dataobject', '{
  "type": "object",
  "properties": {
    "uren": {
      "type": "number"
    }
  },
  "required": [
    "uren"
  ]
}');

/* add producttype*/
INSERT INTO public.producttypen_producttype(id, uuid, gepubliceerd, aanmaak_datum, update_datum, code, toegestane_statussen, keywords, interne_opmerkingen, dataobject_schema_id, uniforme_product_naam_id, verbruiksobject_schema_id)
VALUES (1,'dee273e9-2aa8-40ae-84b7-cb7da3c075ba', true, now(), now(), 'PARKEREN', '{gereed}', '{parkeren, ibs}', '', 2, 1, 1);

/* add zaaktype */
INSERT INTO public.producttypen_zaaktype(id, uuid, producttype_id)
VALUES (1, '744ca059-f412-49d4-8963-5800e4afd486', 1);

/* add externe code */
INSERT INTO public.producttypen_externecode(id, uuid, naam, code, producttype_id)
VALUES (1, '418317dc-c5b8-4690-b309-846bdb13e680', 'ISO', '123', 1),
       (2, '3e3bf0f6-a3f5-469e-8693-c7665d97ba05', 'taak_eigenaar', 'RMWA', 1);

/* add producttype translation*/
INSERT INTO public.producttypen_producttypetranslation(id, language_code, naam, samenvatting, master_id)
VALUES (1, 'nl', 'Parkeren', 'samenvatting translatie', 1),
       (2, 'en', 'Parking', 'samenvatting translatie', 1);

/* add actie*/
INSERT INTO public.producttypen_actie(id, uuid, naam, dmn_tabel_id, dmn_config_id, producttype_id)
VALUES ( 1,'082d143f-6a53-4e08-bc3c-0488b3b490e4', 'watkanikregelen-parkeren', 'alg-parkeren', 1, 1),
       ( 2,'2435b986-7742-4cef-91f2-e1162c2f19c9', 'watkanikregelen-belastingen', 'alg-belastingen', 1, 1);

/* add parameter */
INSERT INTO public.producttypen_parameter(id, uuid, naam, waarde, producttype_id)
VALUES (1, '023ea1f2-8e8c-42d8-9bed-a4ad4edc03c2', 'paymentcategorie', 'parkeren', 1);

/* add contentelement */
INSERT INTO public.producttypen_contentelement(id, "order", uuid, producttype_id)
VALUES (1, 1, 'bcc4ba4e-8022-447f-8ffd-d65781a51d24', 1);

/* add contentlabel */
INSERT INTO public.producttypen_contentlabel(id, uuid, naam)
VALUES (1, '6d9cab7b-311e-44b7-828e-eabbdf139724', 'naam');

/* add contentelementtranslation */
INSERT INTO public.producttypen_contentelementtranslation(id, language_code, content, master_id)
VALUES (1, 'nl', 'test data', 1),
       (2, 'en', 'test data English', 1);

/* add contentelement_labels */
INSERT INTO public.producttypen_contentelement_labels(contentelement_id, contentlabel_id)
VALUES (1, 1);

/* add links */
INSERT INTO public.producttypen_link(id, uuid, naam, url, producttype_id)
VALUES (1, '1a619662-9b0d-4219-81f4-d738fe76850d', 'link naar website', 'https://www.google.com/', 1),
       (2, '7506562c-9a2a-4931-85cd-2c752cbabaf0', 'link naar Ritense website', 'https://ritense.com/', 1);


/* add prijs */
INSERT INTO public.producttypen_prijs(id, uuid, actief_vanaf, producttype_id)
VALUES (1, '317ab929-5cb4-4dde-ae4a-489f4d388699', now(), 1);

/* add prijsoptie */
INSERT INTO public.producttypen_prijsoptie(id, uuid, bedrag, beschrijving, prijs_id)
VALUES (1, 'd1eb87b9-0346-426b-bf7f-a2fb3f47f653', 100.00, 'prijs optie regel 1', 1);

/* add prijsregel */
INSERT INTO public.producttypen_prijsregel(id, uuid, beschrijving, dmn_tabel_id, dmn_config_id, prijs_id)
VALUES (1, '830dda6f-d167-4485-ab99-ebb8f3a33bd3', 'prijs optie regel 1', 'alg-parkeren', 1, 1),
       (2, '04d0f3ab-c691-408d-b99f-a7e045c82dea', 'prijs optie regel 2', 'alg-belastingen', 1, 1);

/*
 add Organisaties
 */
INSERT INTO public.locaties_organisatie(id, uuid, naam, email, telefoonnummer, straat, huisnummer, postcode, stad, code)
VALUES (1, 'ac1ee933-8f9a-414b-bf0c-11adc969ed2c', 'Ritense', 'info@ritsense.nl', '0201234567', 'straat', '1', '1000AB', 'Amsterdam', 'RIT');

/*
 add contacten
 */
INSERT INTO public.locaties_contact(id, uuid, voornaam, achternaam, email, telefoonnummer, rol, organisatie_id)
VALUES (1, '09cd7326-1bf0-4890-b8d1-0ac3ee22bac0', 'Vincent', 'van Beek', 'test@test.nl', '0201234567', 'owner', 1);

/* add locaties */
INSERT INTO public.locaties_locatie(id, uuid, naam, email, telefoonnummer, straat, huisnummer, postcode, stad)
VALUES(1, 'ea3069ae-7ec0-4663-91b7-cab404cc450d','Ritense', 'info@ritsense.nl', '0201234567', 'straat', '1', '1000AB', 'Amsterdam');

/* add producttype_thema */
INSERT INTO public.producttypen_producttype_themas(producttype_id, thema_id)
VALUES (1, 3);

/* add producttype_organisaties */
INSERT INTO public.producttypen_producttype_organisaties(producttype_id, organisatie_id)
VALUES (1, 1);

/* add producttype_contacten */
INSERT INTO public.producttypen_producttype_contacten(producttype_id, contact_id)
VALUES (1, 1);

/* add producttype_locaties */
INSERT INTO public.producttypen_producttype_locaties(producttype_id, locatie_id)
VALUES (1, 1);

/*set sequences */
SELECT pg_catalog.setval('public.producttypen_thema_id_seq', 4, true);

SELECT pg_catalog.setval('public.producttypen_jsonschema_id_seq', 2, true);

SELECT pg_catalog.setval('public.producttypen_uniformeproductnaam_id_seq', 2, true);

SELECT pg_catalog.setval('public.producttypen_producttype_id_seq', 1, true);

SELECT pg_catalog.setval('public.producttypen_zaaktype_id_seq', 1, true);

SELECT pg_catalog.setval('public.producttypen_externecode_id_seq', 2, true);

SELECT pg_catalog.setval('public.producttypen_producttypetranslation_id_seq', 2, true);

SELECT pg_catalog.setval('public.producttypen_actie_id_seq', 2, true);

SELECT pg_catalog.setval('public.producttypen_parameter_id_seq', 1, true);

SELECT pg_catalog.setval('public.producttypen_contentelement_id_seq', 1, true);

SELECT pg_catalog.setval('public.producttypen_contentlabel_id_seq', 1, true);

SELECT pg_catalog.setval('public.producttypen_contentelementtranslation_id_seq', 2, true);

SELECT pg_catalog.setval('public.producttypen_contentelement_labels_id_seq', 1, true);

SELECT pg_catalog.setval('public.producttypen_link_id_seq', 2, true);

SELECT pg_catalog.setval('public.producttypen_prijs_id_seq', 1, true);

SELECT pg_catalog.setval('public.producttypen_prijsoptie_id_seq', 1, true);

SELECT pg_catalog.setval('public.producttypen_prijsregel_id_seq', 2, true);

SELECT pg_catalog.setval('public.producttypen_producttype_themas_id_seq', 1, true);

SELECT pg_catalog.setval('public.producttypen_producttype_organisaties_id_seq', 1, true);

SELECT pg_catalog.setval('public.producttypen_producttype_locaties_id_seq', 1, true);

SELECT pg_catalog.setval('public.producttypen_producttype_contacten_id_seq', 1, true);

SELECT pg_catalog.setval('public.locaties_locatie_id_seq', 1, true);

SELECT pg_catalog.setval('public.locaties_organisatie_id_seq', 1, true);

SELECT pg_catalog.setval('public.locaties_contact_id_seq', 1, true);
