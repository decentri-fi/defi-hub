package io.defitrack.price

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
        prodServer.setUrl("https://api.decentri.fi/price")
        prodServer.setDescription("Server URL in Production environment")

        val info = Info()
            .title("Price API")
            .version("1.0")
            .description("This API exposes the defi api related to balance and token balance operations.")

        return OpenAPI().info(info).servers(listOf(prodServer))
    }
}