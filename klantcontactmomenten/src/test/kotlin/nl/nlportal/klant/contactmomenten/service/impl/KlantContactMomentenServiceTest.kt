package nl.nlportal.klant.contactmomenten.service.impl

import com.ritense.portal.commonground.authentication.JwtBuilder
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import nl.nlportal.klant.contactmomenten.client.KlantContactMomentenClient
import nl.nlportal.klant.contactmomenten.domain.ContactMoment
import nl.nlportal.klant.generiek.domain.ResultPage
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`

@ExperimentalCoroutinesApi
internal class KlantContactMomentenServiceTest {

    var klantContactMomentenClient = mock(KlantContactMomentenClient::class.java)
    var klantContactMomentenService = KlantContactMomentenService(klantContactMomentenClient)

    @Test
    fun `get klantcontactmomenten`() = runBlockingTest {
        val authentication = JwtBuilder().aanvragerBsn("123").buildBurgerAuthentication()
        `when`(klantContactMomentenClient.getContactMomenten(authentication, "http://dummy.nl", 1)).thenReturn(
            ResultPage(
                1,
                null,
                null,
                listOf(
                    mock(ContactMoment::class.java)
                )
            )
        )

        val result = klantContactMomentenService.getKlantContactMomenten(authentication, "http://dummy.nl", 1)

        verify(klantContactMomentenClient, times(1)).getContactMomenten(authentication, "http://dummy.nl", 1)

        assertEquals(1, result.content.size)
    }
}