package io.defitrack.protocol.beefy

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import io.defitrack.common.network.Network
import io.defitrack.common.utils.AsyncUtils.lazyAsync
import io.defitrack.protocol.beefy.domain.BeefyLaunchPool
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import jakarta.annotation.PostConstruct
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.springframework.stereotype.Service

@Service
class BeefyBoostService(
    private val objectMapper: ObjectMapper,
    private val client: HttpClient
) {

    val beefyMapping = mapOf(
        Network.POLYGON to "polygon",
        Network.ARBITRUM to "arbitrum",
        Network.OPTIMISM to "optimism",
        Network.ETHEREUM to "ethereum",
        Network.POLYGON_ZKEVM to "zkevm",
        Network.BASE to "base"
    )

    private val beefyBoosts = lazyAsync {
        load()
    }

    suspend fun load(): MutableMap<Network, List<BeefyLaunchPool>> = withContext(Dispatchers.IO) {
        val retVal: MutableMap<Network, List<BeefyLaunchPool>> = mutableMapOf()

        val vaultsAsList: String =
            client.get("https://api.beefy.finance/boosts").bodyAsText()
        val vaults = objectMapper.readValue(vaultsAsList, object : TypeReference<List<BeefyLaunchPool>>() {})

        beefyMapping.forEach { (network, chain) ->
            retVal[network] = vaults.filter {
                it.chain == chain
            }
        }
        retVal
    }

    suspend fun getBoosts(network: Network): List<BeefyLaunchPool> {
        return beefyBoosts.await().getOrDefault(network, emptyList())
    }
}