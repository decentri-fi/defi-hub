package io.defitrack.protocol.application.beefy.apy

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import io.defitrack.architecture.conditional.ConditionalOnCompany
import io.defitrack.protocol.Company
import io.github.reactivecircus.cache4k.Cache
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.stereotype.Component
import java.math.BigDecimal
import kotlin.time.Duration.Companion.hours

@Component
@ConditionalOnCompany(Company.BEEFY)
class BeefyAPYService(
    private val objectMapper: ObjectMapper,
    private val client: HttpClient
) {

    private val beefyAPIEndpoint: String = "https://api.beefy.finance"

    val cache = Cache.Builder<String, Map<String, BigDecimal>>()
        .expireAfterWrite(1.hours)
        .build()

    suspend fun getAPYS(): Map<String, BigDecimal> {
        return withContext(Dispatchers.IO) {
            cache.get("beefy-api-apys") {
                val result: String = client.get(with(HttpRequestBuilder()) {
                    url("$beefyAPIEndpoint/apy")
                    this
                }).bodyAsText()
                objectMapper.readValue(
                    result,
                    object : TypeReference<Map<String, BigDecimal>>() {})
            }
        }
    }
}
