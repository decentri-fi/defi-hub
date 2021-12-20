package io.defitrack.humandao.distribution.service

import com.fasterxml.jackson.databind.ObjectMapper
import io.defitrack.abi.ABIResource
import io.defitrack.common.network.Network
import io.defitrack.ethereum.config.EthereumContractAccessor
import io.defitrack.ethereumbased.contract.ERC20Contract
import io.defitrack.humandao.distribution.contract.BonusDistributionContract
import io.defitrack.humandao.distribution.vo.BonusDistributionStatus
import io.defitrack.polygon.config.PolygonContractAccessor
import io.defitrack.polygon.config.PolygonMumbaiContractAccessor
import io.ktor.client.*
import io.ktor.client.request.*
import kotlinx.coroutines.runBlocking
import org.bouncycastle.util.encoders.Hex
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.math.BigInteger
import java.math.RoundingMode

@Component
class BonusDistributionService(
    private val client: HttpClient,
    private val objectMapper: ObjectMapper,
    private val polygonContractAccessor: PolygonContractAccessor,
    private val ethereumContractAccessor: EthereumContractAccessor,
    private val mumbaiContractAccessor: PolygonMumbaiContractAccessor,
    private val abiResource: ABIResource
) {

    final val bonusDistributorABI by lazy {
        abiResource.getABI("humandao/HumanDaoDistributor.json")
    }

    final val erc20ABI by lazy {
        abiResource.getABI("general/ERC20.json")
    }

    companion object {
        fun noBonus(address: String): BonusDistributionStatus {
            return BonusDistributionStatus(
                beneficiary = false,
                address = address,
                claimed = false,
                index = 0,
                proof = emptyArray(),
                maxBonusAmount = BigInteger.ZERO,
                currentBonusAmount = BigInteger.ZERO,
                shouldFillUpBalance = false,
            )
        }
    }

    val merkleMap: Map<Network, MerkleConfig> = mapOf(
        Network.POLYGON to fetchMerkleConfig(Network.POLYGON),
        Network.POLYGON_MUMBAI to fetchMerkleConfig(Network.POLYGON_MUMBAI),
        Network.ETHEREUM to fetchMerkleConfig(Network.ETHEREUM),
    )

    val erc20Map: Map<Network, ERC20Contract> = mapOf(
        Network.POLYGON to ERC20Contract(
            polygonContractAccessor,
            erc20ABI,
            "0x72928d5436ff65e57f72d5566dcd3baedc649a88"
        ),
        Network.ETHEREUM to ERC20Contract(
            ethereumContractAccessor,
            erc20ABI,
            "0xdac657ffd44a3b9d8aba8749830bf14beb66ff2d"
        )        ,
        Network.POLYGON_MUMBAI to ERC20Contract(
            mumbaiContractAccessor,
            erc20ABI,
            "0xf8afb97235074ab1d2bb574df577d2b89519f330"
        )
    )

    val bonusDistributorContractMap: Map<Network, BonusDistributionContract> = mapOf(
        Network.POLYGON to BonusDistributionContract(
            polygonContractAccessor,
            bonusDistributorABI,
            "0x5d04ec89c918383fb0810f2ad6c956cb2e41b3db"
        ),
        Network.POLYGON_MUMBAI to BonusDistributionContract(
            mumbaiContractAccessor,
            bonusDistributorABI,
            "0x7fcA16Cb535DEf014b8984e9AAE55f2c23DB8C2f"
        )
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
            return config.claims.entries.find {
                it.key.lowercase() == address.lowercase()
            }?.let {
                val maxBonus = BigInteger(
                    Hex.decode(it.value.amount.substring(2))
                )
                val currentBonus = calculateFromChain(
                    network,
                    address,
                    maxBonus,
                )
                BonusDistributionStatus(
                    beneficiary = true,
                    address = address,
                    claimed = getClaimed(network, it.value.index),
                    index = it.value.index,
                    proof = it.value.proof,
                    maxBonusAmount = maxBonus,
                    currentBonusAmount = currentBonus,
                    shouldFillUpBalance = maxBonus > currentBonus
                )
            } ?: noBonus(address)
        }
    }

    private fun getClaimed(network: Network, index: Int): Boolean {
        return bonusDistributorContractMap[network]?.isClaimed(index.toLong()) ?: false
    }

    private fun calculateFromChain(network: Network, address: String, maxBonusAmount: BigInteger): BigInteger {
        return erc20Map[network]?.let { contract ->
            val balance = contract.balanceOf(address)
            val currentBonus = balance.toBigDecimal().divide(BigDecimal(5), 18, RoundingMode.HALF_UP)
            if (currentBonus > maxBonusAmount.toBigDecimal()) {
                maxBonusAmount
            } else {
                currentBonus.toBigInteger()
            }
        } ?: BigInteger.ZERO
    }
}