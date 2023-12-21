package io.defitrack.protocol.beefy

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import io.defitrack.common.network.Network
import io.defitrack.protocol.beefy.domain.BeefyVault
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.coroutines.runBlocking
import org.springframework.stereotype.Service
import jakarta.annotation.PostConstruct
import org.slf4j.LoggerFactory
import kotlin.math.log

@Service
class BeefyVaultService(
    private val objectMapper: ObjectMapper,
    private val client: HttpClient
) {

    private val logger = LoggerFactory.getLogger(this::class.java)

    val beefyVaults: MutableMap<Network, List<BeefyVault>> = mutableMapOf()

    @PostConstruct
    fun startup() {
        logger.info("Fetching Beefy Vaults from beefy api")
        runBlocking {
            val vaultsAsList: String =
                client.get("https://api.beefy.finance/vaults").bodyAsText()
            val vaults = objectMapper.readValue(vaultsAsList, object : TypeReference<List<BeefyVault>>() {})

            //TODO: refresh
            //TODO: dynamically support the networks
            beefyVaults[Network.POLYGON] = vaults.filter {
                it.chain == "polygon"
            }

            beefyVaults[Network.ARBITRUM] = vaults.filter {
                it.chain == "arbitrum"
            }

            beefyVaults[Network.OPTIMISM] = vaults.filter {
                it.chain == "optimism"
            }

            beefyVaults[Network.ETHEREUM] = vaults.filter {
                it.chain == "ethereum"
            }

            beefyVaults[Network.POLYGON_ZKEVM] = vaults.filter {
                it.chain == "zkevm"
            }

            beefyVaults[Network.BASE] = vaults.filter {
                it.chain == "base"
            }
        }
    }

    fun getVaults(network: Network): List<BeefyVault> {
        return beefyVaults.getOrDefault(network, emptyList())
    }
}