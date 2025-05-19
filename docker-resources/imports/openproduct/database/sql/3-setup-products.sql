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
/* add product */
INSERT INTO public.producten_product(id, uuid, gepubliceerd, aanmaak_datum, update_datum, start_datum, eind_datum, status, prijs, frequentie, verbruiksobject, dataobject, producttype_id)
VALUES (1, '694242af-d906-470b-b7e1-eb3527886854', true, now(), now(), now(),  now()+ INTERVAL '365 DAYS', 'actief', '20.20', 'eenmalig', '{
  "data":{
    "periodes":[
      {
        "did":"2ca085e5-6631-60aa-e063-223c0b0a055d",
        "status":"actief",
        "kenteken":"DB754K",
        "datetimeEnd":"2025-01-27T00:00:00.000Z",
        "datetimeStart":"2025-01-26T12:00:00.000Z"
      }
    ],
    "kentekens":[
      "DB754K"
    ],
    "beschikbareDagdelen":"39"
  },
  "soort":"milieuzoneOldtimerPeriodes",
  "productInstantie":"62aa1bbb-4055-4df9-91e6-1c6171348044"
}', null, 1),
       (2, '55633677-fef4-4ed2-99a4-13efc3360448', true, now(), now(), now(),  now()+ INTERVAL '365 DAYS', 'actief', '20.20', 'eenmalig', null, null, 1),
       (3, '55515634-bf7d-4f2e-8b30-6bbcc0f8bfdc', true, now(), now(), now(),  now()+ INTERVAL '365 DAYS', 'actief', '20.20', 'eenmalig', null, null, 1),
       (4, 'b4addadd-71df-45a9-8559-fca53fc79339', true, now(), now(), now(),  now()+ INTERVAL '365 DAYS', 'actief', '20.20', 'eenmalig', null, null, 1);
/* add eigenaar */
INSERT INTO public.producten_eigenaar(id, uuid, bsn, kvk_nummer, vestigingsnummer, klantnummer, product_id)
VALUES (1, '104dd54c-4464-45f2-9781-9df2c59a35ed', '569312863', '', '', '', 1),
       (2, 'bae857cb-c6b0-48f5-88a8-f94e61092a4e', '', '14127293', '', '', 2),
       (3, '11cca089-5a96-48d7-8fd1-0517a63dbc81', '', '14127293', '000037143557', '', 3);


/*set sequences */
SELECT pg_catalog.setval('public.producten_product_id_seq', 4, true);

SELECT pg_catalog.setval('public.producten_eigenaar_id_seq', 3, true);
