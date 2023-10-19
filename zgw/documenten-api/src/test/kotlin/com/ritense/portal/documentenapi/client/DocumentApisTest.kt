package com.ritense.portal.documentenapi.client

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.BeforeAll
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.junit.jupiter.api.TestInstance
import kotlin.test.assertEquals

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class DocumentApisTest(
    @Autowired val documentApisConfig: DocumentApisConfig
) {

    @BeforeAll
    fun setUp() {
        documentApisConfig.documentapis.get(0).url = "https://localhost:8001/"
        documentApisConfig.documentapis.get(1).url = "https://example.org/"
    }

    @Test
    fun `default config should resolve to default config`() {
        assertEquals(documentApisConfig.getDefault().default, true)
        assertEquals(documentApisConfig.getDefault().url, "https://localhost:8001/")
        assertEquals(documentApisConfig.getDefault().secret, "e09b8bc5-5831-4618-ab28-41411304309d")
    }

    @Test
    fun `empty documentapi should resolve to default config`() {
        assertEquals(documentApisConfig.getConfig("").default, true)
        assertEquals(documentApisConfig.getDefault().url, "https://localhost:8001/")
        assertEquals(documentApisConfig.getConfig("").secret, "e09b8bc5-5831-4618-ab28-41411304309d")
    }

    @Test
    fun `localhost documentapi should resolve to localhost config`() {
        assertEquals(documentApisConfig.getConfig("localhost").default, true)
        assertEquals(documentApisConfig.getDefault().url, "https://localhost:8001/")
        assertEquals(documentApisConfig.getConfig("localhost").secret, "e09b8bc5-5831-4618-ab28-41411304309d")
    }

    @Test
    fun `exampleorg documentapi should resolve to localhost config`() {
        assertEquals(documentApisConfig.getConfig("example.org").default, false)
        assertEquals(documentApisConfig.getConfig("example.org").url, "https://example.org/")
        assertEquals(documentApisConfig.getConfig("example.org").secret, "e09b8bc5-5831-4618-ab28-111111111111")
    }
}