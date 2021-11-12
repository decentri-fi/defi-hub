package io.defitrack.abi

import io.ktor.client.*
import io.ktor.client.request.*
import kotlinx.coroutines.runBlocking
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Component

@Component
class AbiService(private val client: HttpClient) {

    @Cacheable(cacheNames = ["abis"], key = "#id")
    fun getABI(id: String): String {
        return runBlocking {
            client.get("https://raw.githubusercontent.com/defitrack/data/master/abi/${id}")
        }
    }
}