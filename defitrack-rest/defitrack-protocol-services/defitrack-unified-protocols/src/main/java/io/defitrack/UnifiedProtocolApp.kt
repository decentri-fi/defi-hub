package io.defitrack

import io.defitrack.protocol.Company
import io.defitrack.protocol.Protocol
import jakarta.annotation.PostConstruct
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cache.annotation.EnableCaching
import org.springframework.context.ApplicationContext
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.scheduling.annotation.EnableScheduling

class UnifiedProtocolApp(
    @Value("\${decentrifi.company}") private val company: String,
    applicationContext: ApplicationContext
) : ProtocolApplication(applicationContext) {

    private val logger = LoggerFactory.getLogger(this::class.java)

    @PostConstruct
    fun init() {
        logger.info("Starting ${getCompany().name} protocol")
    }

    override fun getCompany(): Company {
        return Company.entries.firstOrNull {
            it.slug.lowercase() == company.lowercase() || it.name.lowercase() == company.lowercase()
        } ?: throw IllegalArgumentException("Company $company not found")
    }
}

fun main(args: Array<String>) {
    runApplication<UnifiedProtocolApp>(*args)
}