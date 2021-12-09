package io.defitrack.humandao.distribution.service

import com.fasterxml.jackson.databind.ObjectMapper
import io.defitrack.common.network.Network
import io.defitrack.humandao.distribution.vo.BonusDistributionStatus
import io.ktor.client.*
import io.ktor.client.request.*
import kotlinx.coroutines.runBlocking
import org.bouncycastle.util.encoders.Hex
import org.springframework.stereotype.Component
import java.math.BigInteger

@Component
class BonusDistributionService(private val client: HttpClient,
                               private val objectMapper: ObjectMapper
) {

    companion object {
        fun noBonus(address: String): BonusDistributionStatus {
            return BonusDistributionStatus(
                beneficiary = false,
                address = address,
                claimed = false,
                index = 0,
                proof = emptyArray(),
                amount = BigInteger.ZERO
            )
        }
    }

    val merkleMap: Map<Network, MerkleConfig> = mapOf(
        Network.POLYGON to fetchMerkleConfig(Network.POLYGON),
        Network.ETHEREUM to fetchMerkleConfig(Network.ETHEREUM),
    )

    private fun fetchMerkleConfig(network: Network): MerkleConfig {
        return runBlocking {
            val url =
                "https://raw.githubusercontent.com/humandao-org/BonusDistributorContracts/master/data/${network.slug}/merkle.json"
            val result: String = client.get(url)
            objectMapper.readValue(result, MerkleConfig::class.java)
        }
    }

    fun getBonusDistributionStatus(network: Network, address: String): BonusDistributionStatus {
        val config = merkleMap[network]
        if (config == null) {
            return noBonus(address)
        } else {
             return  config.claims.entries.find {
                it.key.lowercase() == address.lowercase()
            }?.let {
                 BonusDistributionStatus(
                     beneficiary = true,
                     address = address,
                     claimed = false,
                     index = it.value.index,
                     it.value.proof,
                     BigInteger(
                         Hex.decode(it.value.amount.substring(2))
                     )
                 )
            } ?:  noBonus(address)
        }
    }
}