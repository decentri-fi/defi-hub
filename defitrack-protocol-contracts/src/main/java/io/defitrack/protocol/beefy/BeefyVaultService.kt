package io.defitrack.protocol.beefy

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import io.defitrack.common.network.Network
import io.defitrack.common.utils.AsyncUtils.lazyAsync
import io.defitrack.protocol.beefy.domain.BeefyVault
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.coroutines.runBlocking
import org.springframework.stereotype.Service
import jakarta.annotation.PostConstruct
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory
import kotlin.math.log

@Service
class BeefyVaultService(
    private val objectMapper: ObjectMapper,
    private val client: HttpClient
) {

    private val logger = LoggerFactory.getLogger(this::class.java)

    val beefyVaults = lazyAsync {
        load()
    }

    suspend fun load(): MutableMap<Network, List<BeefyVault>> = withContext(Dispatchers.IO) {
        logger.info("Fetching Beefy Vaults from beefy api")
        val retVal: MutableMap<Network, List<BeefyVault>> = mutableMapOf()

        val vaultsAsList: String =
            client.get("https://api.beefy.finance/vaults").bodyAsText()
        val vaults = objectMapper.readValue(vaultsAsList, object : TypeReference<List<BeefyVault>>() {})

        retVal[Network.POLYGON] = vaults.filter {
            it.chain == "polygon"
        }

        retVal[Network.ARBITRUM] = vaults.filter {
            it.chain == "arbitrum"
        }

        retVal[Network.OPTIMISM] = vaults.filter {
            it.chain == "optimism"
        }

        retVal[Network.ETHEREUM] = vaults.filter {
            it.chain == "ethereum"
        }

        retVal[Network.POLYGON_ZKEVM] = vaults.filter {
            it.chain == "zkevm"
        }

        retVal[Network.BASE] = vaults.filter {
            it.chain == "base"
        }
        retVal
    }

    suspend fun getVaults(network: Network): List<BeefyVault> {
        return beefyVaults.await().getOrDefault(network, emptyList())
    }
}