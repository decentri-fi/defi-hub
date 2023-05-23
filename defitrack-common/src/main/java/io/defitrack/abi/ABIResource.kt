package io.defitrack.abi

import io.github.reactivecircus.cache4k.Cache
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class ABIResource(
    @Value("\${abiResourceLocation:http://defitrack-abi:8080}") val abiResourceLocation: String,
    private val client: HttpClient
) {

    val cache = Cache.Builder<String, String>().build()

    suspend fun getABI(abi: String): String {
        return cache.get(abi) {
            withContext(Dispatchers.IO) {
                val response: Abi = client.get("$abiResourceLocation?id=${abi}").body()
                response.content
            }
        }
    }
}