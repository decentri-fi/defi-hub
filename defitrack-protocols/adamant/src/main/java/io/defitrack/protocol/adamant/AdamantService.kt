package io.defitrack.protocol.adamant

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import kotlinx.coroutines.runBlocking
import org.springframework.stereotype.Component

@Component
class AdamantService(
    private val objectMapper: ObjectMapper,
    private val client: HttpClient
) {

    val vaultListLocation = "https://raw.githubusercontent.com/eepdev/vaults/main/current_vaults.json"

    suspend fun adamantGenericVaults(): List<AdamantVault> {
        val response: String = client.get(vaultListLocation).body()
        return objectMapper.readValue(response,
            object : TypeReference<List<AdamantVault>>() {

            }).filter {
            !it.poolName.startsWith("ETH (") //these are different types of vaults
        }
    }
}