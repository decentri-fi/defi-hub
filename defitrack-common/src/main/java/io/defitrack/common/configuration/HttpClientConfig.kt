package io.defitrack.common.configuration

import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.http.*
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

import io.ktor.client.engine.okhttp.*
@Configuration
class HttpClientConfig {

    @Bean
    fun provideClient(): HttpClient {
        return HttpClient(OkHttp) {
            install(HttpTimeout) {
                requestTimeoutMillis = 120000
                connectTimeoutMillis = 60000
                socketTimeoutMillis = 120000
            }
        }
    }
}