package io.defitrack

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.servers.Server
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class OpenAPIConfig {

    @Bean
    fun myOpenAPI(): OpenAPI {
        val prodServer = Server()
        prodServer.setUrl("https://api.decentri.fi/ens")
        prodServer.setDescription("Server URL in Production environment")

        val info = Info()
            .title("ENS API")
            .version("1.0")
            .description("This API exposes the defi api related to ENS.")

        return OpenAPI().info(info).servers(listOf(prodServer))
    }
}