package io.defitrack.protocol.idex

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.stereotype.Component

@Component
class IdexService(private val client: HttpClient) {

    companion object {
        const val idexAPI = "https://api-matic.idex.io"
    }

    suspend fun getLPs(): List<IdexLP> = withContext(Dispatchers.IO) {
        client.get("$idexAPI/v1/liquidityPools").body()
    }

    fun idexFarm(): List<String> {
        return listOf("0xb9c951c85c8646dafcdc0ad99d326c621abbadce")
    }
}