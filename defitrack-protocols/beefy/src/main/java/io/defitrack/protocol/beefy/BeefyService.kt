package io.defitrack.protocol.beefy

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import io.defitrack.protocol.beefy.domain.BeefyVault
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import kotlinx.coroutines.runBlocking
import org.springframework.stereotype.Service
import javax.annotation.PostConstruct

@Service
class BeefyService(
    private val objectMapper: ObjectMapper,
    private val client: HttpClient
) {

    val beefyPolygonVaults: MutableList<BeefyVault> = mutableListOf()
    val beefyArbitrumVaults: MutableList<BeefyVault> = mutableListOf()
    val beefyBscVaults: MutableList<BeefyVault> = mutableListOf()
    val beefyFantomVaults: MutableList<BeefyVault> = mutableListOf()

    @PostConstruct
    fun startup() {
        runBlocking {
            val vaultsAsList: String =
                client.get("https://api.beefy.finance/vaults").body()
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

            beefyBscVaults.addAll(
                vaults.filter {
                    it.chain == "bsc"
                }
            )

            beefyFantomVaults.addAll(
                vaults.filter {
                    it.chain == "fantom"
                }
            )
        }
    }
}