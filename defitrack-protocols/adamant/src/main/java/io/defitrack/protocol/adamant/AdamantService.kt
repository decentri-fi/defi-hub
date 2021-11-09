package io.defitrack.protocol.adamant

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.runBlocking
import org.springframework.stereotype.Component

@Component
class AdamantService(
    private val objectMapper: ObjectMapper,
    private val client: HttpClient
) {

    val vaultListLocation = "https://raw.githubusercontent.com/eepdev/vaults/main/current_vaults.json"

    fun adamantGenericVaults(): List<AdamantVault> = runBlocking {
        val response = client.get<String>(vaultListLocation)
        return@runBlocking objectMapper.readValue(response,
            object : TypeReference<List<AdamantVault>>() {

            })
    }

    fun query(query: String): String = runBlocking {
        client.request(vaultListLocation) {
            method = HttpMethod.Post
            body = objectMapper.writeValueAsString(mapOf("query" to query))
        }
    }
}