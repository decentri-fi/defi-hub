package io.defitrack

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import io.defitrack.company.CompaniesProvider
import io.defitrack.market.pooling.vo.PoolingMarketVO
import io.defitrack.protocol.Company
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import jakarta.annotation.PostConstruct
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.availability.AvailabilityChangeEvent
import org.springframework.boot.availability.ReadinessState
import org.springframework.boot.runApplication
import org.springframework.cache.annotation.EnableCaching
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableCaching
@EnableScheduling
@EnableAsync
class UnifiedProtocolApp(
    @Value("\${decentrifi.companies}") private val companies: List<String>,
    private val objectMapper: ObjectMapper,
    applicationContext: ApplicationContext
) {

    init {
        AvailabilityChangeEvent.publish(applicationContext, ReadinessState.REFUSING_TRAFFIC)
    }

    private val logger = LoggerFactory.getLogger(this::class.java)

    @PostConstruct
    fun init() {
        logger.info("Starting protocol application for companies: ${companies}")
    }

    @Bean
    fun provideCompany(): CompaniesProvider {
        return object : CompaniesProvider {
            override fun getCompanies(): List<Company> {
                return this@UnifiedProtocolApp.getCompanies()
            }
        }
    }

    fun getCompanies(): List<Company> {
        val provided = companies.map(String::lowercase)

        return Company.entries.filter {
            provided.contains(it.slug.lowercase()) || provided.contains(it.name.lowercase())
        }
    }
}

fun main(args: Array<String>) {
    runApplication<UnifiedProtocolApp>(*args)
}