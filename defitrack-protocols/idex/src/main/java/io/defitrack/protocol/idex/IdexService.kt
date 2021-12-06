package io.defitrack.protocol.idex

import io.ktor.client.*
import io.ktor.client.request.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.springframework.stereotype.Component

@Component
class IdexService(private val client: HttpClient) {

    companion object {
        const val idexAPI = "https://api-matic.idex.io"
    }

    fun getLPs(): List<IdexLP> = runBlocking(Dispatchers.IO) {
        client.get("$idexAPI/v1/liquidityPools")
    }

    fun idexFarm(): String {
        return "0xb9c951c85c8646dafcdc0ad99d326c621abbadce"
    }
}