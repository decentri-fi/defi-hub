package io.defitrack.protocol.adamant

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import org.springframework.stereotype.Component

@Component
class AdamantService(
    private val objectMapper: ObjectMapper,
    private val client: HttpClient
) {

    val vaultListLocation = "https://raw.githubusercontent.com/eepdev/vaults/main/current_vaults.json"

    suspend fun adamantGenericVaults(): List<AdamantVault> {
        val response: String = client.get(vaultListLocation).bodyAsText()
        val vaults: List<AdamantVault> = objectMapper.readValue(response)
        return vaults.filter {
            !it.poolName.startsWith("ETH (") //these are different types of vaults
        }
    }
}