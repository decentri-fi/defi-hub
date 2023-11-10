package io.defitrack.protocol.beefy

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import io.defitrack.protocol.beefy.domain.BeefyLaunchPool
import io.defitrack.protocol.beefy.domain.BeefyVault
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.coroutines.runBlocking
import org.springframework.stereotype.Service
import jakarta.annotation.PostConstruct

@Service
class BeefyBoostService(
    private val objectMapper: ObjectMapper,
    private val client: HttpClient
) {

    val beefyPolygonVaults: MutableList<BeefyLaunchPool> = mutableListOf()
    val beefyArbitrumVaults: MutableList<BeefyLaunchPool> = mutableListOf()
    val beefyOptimismVaults: MutableList<BeefyLaunchPool> = mutableListOf()
    val beefyEthereumVaults: MutableList<BeefyLaunchPool> = mutableListOf()
    val beefyPolygonZkEvmVaults: MutableList<BeefyLaunchPool> = mutableListOf()
    val beefyBaseVaults: MutableList<BeefyLaunchPool> = mutableListOf()

    @PostConstruct
    fun startup() {
        runBlocking {
            val vaultsAsList: String =
                client.get("https://api.beefy.finance/boosts").bodyAsText()
            val vaults = objectMapper.readValue(vaultsAsList, object : TypeReference<List<BeefyLaunchPool>>() {})

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