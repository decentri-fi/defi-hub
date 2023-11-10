package io.defitrack.protocol.beefy

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import io.defitrack.protocol.beefy.domain.BeefyVault
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.coroutines.runBlocking
import org.springframework.stereotype.Service
import jakarta.annotation.PostConstruct

@Service
class BeefyVaultService(
    private val objectMapper: ObjectMapper,
    private val client: HttpClient
) {

    val beefyPolygonVaults: MutableList<BeefyVault> = mutableListOf()
    val beefyArbitrumVaults: MutableList<BeefyVault> = mutableListOf()
    val beefyOptimismVaults: MutableList<BeefyVault> = mutableListOf()
    val beefyEthereumVaults: MutableList<BeefyVault> = mutableListOf()
    val beefyPolygonZkEvmVaults: MutableList<BeefyVault> = mutableListOf()
    val beefyBaseVaults: MutableList<BeefyVault> = mutableListOf()

    @PostConstruct
    fun startup() {
        runBlocking {
            val vaultsAsList: String =
                client.get("https://api.beefy.finance/vaults").bodyAsText()
            val vaults = objectMapper.readValue(vaultsAsList, object : TypeReference<List<BeefyVault>>() {})

            beefyPolygonVaults.addAll(
                vaults.filter {
                    it.chain == "polygon"
                }
            )

            beefyArbitrumVaults.addAll(
                vaults.filter {
                    it.chain == "arbitrum"
                }
            )

            beefyOptimismVaults.addAll(
                vaults.filter {
                    it.chain == "optimism"
                }
            )

            beefyEthereumVaults.addAll(
                vaults.filter {
                    it.chain == "ethereum"
                }
            )

            beefyPolygonZkEvmVaults.addAll(
                vaults.filter {
                    it.chain == "zkevm"
                }
            )

            beefyBaseVaults.addAll(
                vaults.filter {
                    it.chain == "base"
                }
            )
        }
    }
}