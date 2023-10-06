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
        val prodServer = Server();
        prodServer.setUrl("https://api.decentri.fi");
        prodServer.setDescription("Server URL in Production environment");

        val info = Info()
            .title("Tutorial Management API")
            .version("1.0")
            .description("This API exposes endpoints to manage tutorials.")

        return OpenAPI().info(info).servers(listOf(prodServer))
    }
}