INSERT INTO public.token_tokenauth (id, token, contact_person, email, organization, last_modified, created, application, administration, identifier)
VALUES (2, 'ac045222c9e7cde8120b48735560f9b920bb58cd', 'Admin', 'admin@example.com', '', '2024-09-06 07:26:53.703312 +00:00', '2024-09-06 07:26:53.703384 +00:00', '', '', 'test_token');

INSERT INTO public.klantinteracties_partij (id, bezoekadres_nummeraanduiding_id, bezoekadres_adresregel1, bezoekadres_adresregel2, bezoekadres_adresregel3, bezoekadres_land, correspondentieadres_nummeraanduiding_id, correspondentieadres_adresregel1, correspondentieadres_adresregel2, correspondentieadres_adresregel3, correspondentieadres_land, uuid, nummer, interne_notitie, soort_partij, indicatie_geheimhouding, voorkeurstaal, indicatie_actief, voorkeurs_digitaal_adres_id, voorkeurs_rekeningnummer_id, bezoekadres_huisnummer, bezoekadres_huisnummertoevoeging, bezoekadres_postcode, bezoekadres_stad, bezoekadres_straatnaam, correspondentieadres_huisnummer, correspondentieadres_huisnummertoevoeging, correspondentieadres_postcode, correspondentieadres_stad, correspondentieadres_straatnaam)
VALUES
    (100, '', '', '', '', '', '', '', '', '', '', 'c264d2cf-63a7-4280-bcee-6ec30e822769', '11111', '', 'persoon', true, 'nld', true, null, null, 1, '', '', '', '', 1, '', '', '', ''),
    (101, '', '', '', '', '', '', '', '', '', '', '042ed68f-9428-41b8-a8b7-f2bfb1e6bc3d', '22222', '', 'organisatie', true, 'nld', true, null, null, 1, '', '', '', '', 1, '', '', '', '');

INSERT INTO public.klantinteracties_partijidentificator (id, uuid, andere_partij_identificator, partij_identificator_code_objecttype, partij_identificator_code_soort_object_id, partij_identificator_object_id, partij_identificator_code_register, partij_id, sub_identificator_van_id)
VALUES (200, 'fc707204-faf6-4911-9af8-55174808bb94', '', 'natuurlijk_persoon', 'bsn', '569312863', 'brp', 100, null),
       (201, 'b945acd0-7d92-4277-b765-b445b47e5c40', '', 'niet_natuurlijk_persoon', 'kvk_nummer', '14127293', 'hr', 101, null),
       (202, '71cf06c1-70ee-4354-9658-d33b167e873d', '', 'vestiging', 'vestigingsnummer', '000037143557', 'hr', 101, 201);

INSERT INTO public.klantinteracties_persoon (id, contactnaam_voorletters, contactnaam_voornaam, contactnaam_voorvoegsel_achternaam, contactnaam_achternaam, partij_id)
VALUES(900, 'V','Vincent', 'van', 'Beek', 100);

INSERT INTO public.klantinteracties_organisatie (id, naam, partij_id)
VALUES(901, 'Ritense', 101);

INSERT INTO public.klantinteracties_klantcontact (id, uuid, nummer, referentienummer, kanaal, onderwerp, inhoud, indicatie_contact_gelukt, taal, vertrouwelijk, plaatsgevonden_op, reactie, metadata)
VALUES (400, '482a8529-0ebd-4424-83a9-b9f88335673d', '0000000333', '0000000333', 'E-mail', 'Vraag over vergunningsaanvraag', 'Lorem ipsum dolor sit amet, consectetur adipiscing elit. Cras ut aliquam velit.', true, 'nld', true, '2025-03-06 11:02:24+00', 'reactie','{
"test": "test"
}'),
       (401, 'f6b89308-7c91-4ca3-a280-4dc08a69de7c', '0000000334', '0000000334', 'Telefoon', 'Klacht', 'Lorem ipsum dolor sit amet, consectetur adipiscing elit. Cras ut aliquam velit.', true, 'nld', true, '2025-01-06 11:02:24+00', 'reactie','{
"test": "test"
}');
INSERT INTO public.klantinteracties_onderwerpobject(id, onderwerpobjectidentificator_object_id, uuid, klantcontact_id, was_klantcontact_id, onderwerpobjectidentificator_code_objecttype, onderwerpobjectidentificator_code_register, onderwerpobjectidentificator_code_soort_object_id)
VALUES (600, '4a8b9c0d-109f-42d4-910c-bdb6addf47a8', '33489b0e-5954-4fc7-a3fd-d91f00ec0aa3', 400, null, 'zaak', 'open-zaak', 'uuid');

INSERT INTO public.klantinteracties_betrokkene (id, bezoekadres_nummeraanduiding_id, bezoekadres_adresregel1, bezoekadres_adresregel2, bezoekadres_adresregel3, bezoekadres_land, correspondentieadres_nummeraanduiding_id, correspondentieadres_adresregel1, correspondentieadres_adresregel2, correspondentieadres_adresregel3, correspondentieadres_land, contactnaam_voorletters, contactnaam_voornaam, contactnaam_voorvoegsel_achternaam, contactnaam_achternaam, uuid, rol, organisatienaam, initiator, klantcontact_id, partij_id, bezoekadres_huisnummer, bezoekadres_huisnummertoevoeging, bezoekadres_postcode, bezoekadres_stad, bezoekadres_straatnaam, correspondentieadres_huisnummer, correspondentieadres_huisnummertoevoeging, correspondentieadres_postcode, correspondentieadres_stad, correspondentieadres_straatnaam)
VALUES(300, '0000000000000001','damrak 1 1001XX Amsterdam', '', '', 'nl', '0000000000000001', 'damrak 1 1001XX Amsterdam', '', '', 'nl', 'D', 'Donald', '', 'Duck', '7694c26b-8118-43a0-b0c1-37673f0f46e4', 'klant', '', true, 400, 100, 1, '', '', '','', 1, '', '', '', '');

INSERT INTO public.klantinteracties_digitaaladres (id, uuid, soort_digitaal_adres, adres, omschrijving, betrokkene_id, partij_id, is_standaard_adres, referentie, verificatie_datum)
VALUES(500, 'a5f323d8-8c87-43c2-990b-eae03d721adf', 'email', 'test@test.nl', '', 300, 100, true, 'portaal', '2025-08-06'),
      (501, '3b006b45-afa9-44ed-a690-32c545924a28', 'telefoonnummer', '0701234568', '', 300, 100, true, '', null),
      (502, 'da80dc1c-bf9b-449f-80e6-2906e66db0a7', 'telefoonnummer', '0701234567', '', 300, 100, false, 'portaal', null);
