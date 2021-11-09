package io.defitrack.abi

import io.github.reactivecircus.cache4k.Cache
import io.ktor.client.*
import io.ktor.client.request.*
import kotlinx.coroutines.runBlocking
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class ABIResource(
    @Value("\${abiResourceLocation:http://defitrack-abi:8080}") val abiResourceLocation: String,
    private val client: HttpClient) {

    val cache = Cache.Builder().build<String, String>()

    fun getABI(abi: String): String {
        return runBlocking {
            cache.get(abi) {
                client.get<Abi>("$abiResourceLocation?id=${abi}").content
            }
        }
    }
}