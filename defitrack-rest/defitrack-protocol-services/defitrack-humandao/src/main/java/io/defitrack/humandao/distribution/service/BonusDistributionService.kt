package io.defitrack.humandao.distribution.service

import com.fasterxml.jackson.databind.ObjectMapper
import io.defitrack.abi.ABIResource
import io.defitrack.common.network.Network
import io.defitrack.evm.contract.BlockchainGatewayProvider
import io.defitrack.evm.contract.ERC20Contract
import io.defitrack.humandao.distribution.contract.BonusDistributionContract
import io.defitrack.humandao.distribution.vo.BonusDistributionStatus
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.coroutines.runBlocking
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.math.BigInteger
import java.math.RoundingMode

@Component
class BonusDistributionService(
    private val client: HttpClient,
    private val objectMapper: ObjectMapper,
    private val blockchainGatewayProvider: BlockchainGatewayProvider,
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
                maxBonusAmount = "0",
                currentBonusAmount = "0",
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
            blockchainGatewayProvider.getGateway(Network.POLYGON),
            erc20ABI,
            "0x72928d5436ff65e57f72d5566dcd3baedc649a88"
        ),
        Network.ETHEREUM to ERC20Contract(
            blockchainGatewayProvider.getGateway(Network.ETHEREUM),
            erc20ABI,
            "0xdac657ffd44a3b9d8aba8749830bf14beb66ff2d"
        )        ,
        Network.POLYGON_MUMBAI to ERC20Contract(
            blockchainGatewayProvider.getGateway(Network.POLYGON_MUMBAI),
            erc20ABI,
            "0xf8afb97235074ab1d2bb574df577d2b89519f330"
        )
    )

    val bonusDistributorContractMap: Map<Network, BonusDistributionContract> = mapOf(
        Network.POLYGON to BonusDistributionContract(
            blockchainGatewayProvider.getGateway(Network.POLYGON),
            bonusDistributorABI,
            "0x5d04ec89c918383fb0810f2ad6c956cb2e41b3db"
        ),
        Network.ETHEREUM to BonusDistributionContract(
            blockchainGatewayProvider.getGateway(Network.ETHEREUM),
            bonusDistributorABI,
            "0xD53b145739352c1BCc7079cDdA0cf6EDfbd8F015"
        ),
        Network.POLYGON_MUMBAI to BonusDistributionContract(
            blockchainGatewayProvider.getGateway(Network.POLYGON),
            bonusDistributorABI,
            "0x7fcA16Cb535DEf014b8984e9AAE55f2c23DB8C2f"
        )
    )

    private fun fetchMerkleConfig(network: Network): MerkleConfig {
        return runBlocking {
            val url =
                "https://raw.githubusercontent.com/humandao-org/BonusDistributorContracts/master/data/${network.slug}/merkle.json"
            val result: String = client.get(url).bodyAsText()
            objectMapper.readValue(result, MerkleConfig::class.java)
        }
    }

    suspend fun getBonusDistributionStatus(network: Network, address: String): BonusDistributionStatus {
        val config = merkleMap[network]
        if (config == null) {
            return noBonus(address)
        } else {
            return config.claims.entries.find {
                it.key.lowercase() == address.lowercase()
            }?.let {
                val maxBonus = BigInteger(
                    it.value.amount.substring(2), 16
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
                    maxBonusAmount = maxBonus.toString(),
                    currentBonusAmount = currentBonus.toString(),
                    shouldFillUpBalance = maxBonus > currentBonus
                )
            } ?: noBonus(address)
        }
    }

    private suspend fun getClaimed(network: Network, index: Int): Boolean {
        return bonusDistributorContractMap[network]?.isClaimed(index.toLong()) ?: false
    }

    private suspend fun calculateFromChain(network: Network, address: String, maxBonusAmount: BigInteger): BigInteger {
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