package nl.nlportal.klant.contactmomenten.graphql

import com.ritense.portal.commonground.authentication.CommonGroundAuthentication
import com.ritense.portal.graphql.security.SecurityConstants
import graphql.GraphQLContext
import graphql.schema.DataFetchingEnvironment
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import nl.nlportal.klant.contactmomenten.service.impl.KlantContactMomentenService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.springframework.security.core.Authentication

@ExperimentalCoroutinesApi
internal class ContactMomentQueryTest {

    val contactMomentenService = mock(KlantContactMomentenService::class.java)
    val contactMomentQuery = ContactMomentQuery(contactMomentenService)
    var environment = mock(DataFetchingEnvironment::class.java)
    var authentication = mock(CommonGroundAuthentication::class.java)
    val context = mock(GraphQLContext::class.java)

    @BeforeEach
    fun setup() {
        `when`(environment.graphQlContext).thenReturn(context)
        `when`(context.get<Authentication>(SecurityConstants.AUTHENTICATION_KEY)).thenReturn(authentication)
    }

    @Test
    fun getKlantContactMomenten() = runTest {

        `when`(
            contactMomentenService.getKlantContactMomenten(
                authentication,
                "http://dummy.nl",
                1
            )
        ).thenReturn(mock(ContactMomentPage::class.java))

        contactMomentQuery.getKlantContactMomenten(environment, "http://dummy.nl", 1)
        verify(contactMomentenService, times(1))
    }
}