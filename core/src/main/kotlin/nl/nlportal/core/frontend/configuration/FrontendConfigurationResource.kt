package nl.nlportal.core.frontend.configuration

import io.github.oshai.kotlinlogging.KotlinLogging
import nl.nlportal.core.autoconfiguration.CoreThemeConfigurationProperties
import nl.nlportal.core.frontend.service.FrontendConfigurationService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(value = ["/api/public"])
class FrontendConfigurationResource(
    private val frontendConfigurationService: FrontendConfigurationService,
) {

    @GetMapping(value = ["/theme/style"])
    fun style(): ResponseEntity<String> =
        when (val style = frontendConfigurationService.getStyle()) {
            null -> ResponseEntity.noContent().build()
            else -> ResponseEntity.ok().header("Content-Type", "text/css").body(style)
        }

    @GetMapping(value = ["/theme/logo"])
    fun logo(): ResponseEntity<String> =
        when (val logo = frontendConfigurationService.getLogo()) {
            null -> ResponseEntity.noContent().build()
            else -> ResponseEntity.ok().body(logo)
        }
}
