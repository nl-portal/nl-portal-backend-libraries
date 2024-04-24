package nl.nlportal.startform.service

import jakarta.transaction.Transactional
import nl.nlportal.startform.BaseIntegrationTest
import nl.nlportal.startform.domain.StartForm
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.util.*

@Transactional
class StartFormServiceIntTest: BaseIntegrationTest() {

    @Autowired
    lateinit var startFormService: StartFormService

//    @Test
//    fun `create startform`() {
//        val startFormsInDBBefore = startFormService.getAllStartFormDTOs()
//        startFormService.createStartForm(
//            StartForm(
//                id = UUID.randomUUID(),
//                formName = "formName",
//                typeUUID = UUID.randomUUID(),
//                typeVersion = 1
//            )
//        )
//        val startFormsInDBAfter = startFormService.getAllStartFormDTOs()
//        assertThat(startFormsInDBBefore.size).isLessThan(startFormsInDBAfter.size)
//    }
}