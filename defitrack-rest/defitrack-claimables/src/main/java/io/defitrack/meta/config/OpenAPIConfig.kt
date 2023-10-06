package io.defitrack.meta.config

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.servers.Server
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class OpenAPIConfig {

    @Bean
    fun openapi(): OpenAPI {
        return OpenAPI().servers(
            listOf(Server().url("https://claimables.decentri.fi"))
        )
    }
}