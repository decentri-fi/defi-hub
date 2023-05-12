package io.defitrack.protocol

import org.springframework.cloud.gateway.route.RouteLocator
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.server.RequestPredicates.GET
import org.springframework.web.reactive.function.server.RouterFunction
import org.springframework.web.reactive.function.server.RouterFunctions
import org.springframework.web.reactive.function.server.ServerResponse


@Configuration
class CompanyRouterConfig {
    @Bean
    fun companyRoutes(builder: RouteLocatorBuilder): RouteLocator {
        val routeBuilder = builder.routes()

        Company.values().forEach { company ->
            routeBuilder.route(company.name) {
                it.path(true, "/${company.slug}/**")
                    .filters { filter ->
                        filter.rewritePath(
                            "/${company.slug}/(?<segment>.*)",
                            "/\${segment}"
                        )
                    }
                    .uri("http://defitrack-${company.slug}:8080/api")
            }
        }
        return routeBuilder.build()
    }

    @Bean
    fun companiesRoute(companyHandler: CompanyHandler): RouterFunction<ServerResponse> {
        return RouterFunctions
            .route(GET("/companies"), companyHandler::getCompanies)
    }
}