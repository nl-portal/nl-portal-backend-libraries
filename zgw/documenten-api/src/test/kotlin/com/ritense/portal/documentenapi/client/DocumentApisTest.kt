package com.ritense.portal.documentenapi.client

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.junit.jupiter.api.TestInstance
import kotlin.test.assertEquals

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class DocumentApisTest(@Autowired val documentApisConfig: DocumentApisConfig) {

    @Test
    fun `localhost documentapi should resolve to localhost config`() {
        assertEquals("https://localhost:8001/", documentApisConfig.getConfig("openzaak").url)
        assertEquals("e09b8bc5-5831-4618-ab28-41411304309d", documentApisConfig.getConfig("openzaak").secret)
    }

    @Test
    fun `exampleorg documentapi should resolve to localhost config`() {
        assertEquals("https://example.org/", documentApisConfig.getConfig("example").url)
        assertEquals("e09b8bc5-5831-4618-ab28-111111111111", documentApisConfig.getConfig("example").secret)
    }

    @Test
    fun `openzaak url should resolve to openzaak`() {
        assertEquals("openzaak", documentApisConfig.getConfigForDocumentUrl("https://localhost:8001/documenten/api/v1/enkelvoudiginformatieobjecten/5f1e2695-8b68-448a-a62d-531321c744ec"))
    }

    @Test
    fun `exampleorg url should resolve to example`() {
        assertEquals("example", documentApisConfig.getConfigForDocumentUrl("https://example.org/adsf/api/v1/werwer/5f1e2695-8b68-448q"))
    }
}