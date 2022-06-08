package io.defitrack.abi

import io.github.reactivecircus.cache4k.Cache
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import org.springframework.stereotype.Component

@Component
class AbiService(private val client: HttpClient) {

    val cache = Cache.Builder().build<String, String>()

    suspend fun getABI(id: String): String {
        return cache.get(id) {
            client.get("https://raw.githubusercontent.com/defitrack/data/master/abi/${id}").bodyAsText()
        }
    }
}