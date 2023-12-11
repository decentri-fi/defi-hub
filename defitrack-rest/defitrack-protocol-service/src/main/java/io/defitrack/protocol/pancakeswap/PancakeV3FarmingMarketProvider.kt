package io.defitrack.protocol.pancakeswap

import arrow.core.nel
import io.defitrack.abi.TypeUtils
import io.defitrack.abi.TypeUtils.Companion.toUint256
import io.defitrack.abi.TypeUtils.Companion.uint24
import io.defitrack.abi.TypeUtils.Companion.uint256
import io.defitrack.common.network.Network
import io.defitrack.common.utils.Refreshable
import io.defitrack.conditional.ConditionalOnCompany
import io.defitrack.evm.contract.ContractCall
import io.defitrack.market.farming.FarmingMarketProvider
import io.defitrack.market.farming.domain.FarmingMarket
import io.defitrack.protocol.Company
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.sushiswap.contract.MasterChefBasedContract
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.launch
import org.springframework.stereotype.Component
import org.web3j.abi.datatypes.Function
import java.math.BigInteger

@ConditionalOnCompany(Company.PANCAKESWAP)
@Component
class PancakeV3FarmingMarketProvider : FarmingMarketProvider() {

    private val masterchefContractAddress = "0xe9c7f3196ab8c09f6616365e8873daeb207c0391"
    private fun getContract() = object : MasterChefBasedContract(
        "CAKE",
        "pendingCake",
        getBlockchainGateway(),
        masterchefContractAddress
    ) {
        fun pendingCake(tokenId: Long): ContractCall {
            return createFunction(
                "pendingCake",
                tokenId.toBigInteger().toUint256().nel(),
                uint256().nel()
            )
        }

        suspend fun customPoolInfos(): List<PancakePoolInfo> {
            val multicalls = (0 until poolLength.await().toInt()).map { poolIndex ->
                createFunction(
                    "poolInfo",
                    inputs = listOf(poolIndex.toBigInteger().toUint256()),
                    outputs = listOf(
                        uint256(),
                        TypeUtils.address(),
                        TypeUtils.address(),
                        TypeUtils.address(),
                        uint24(),
                        uint256(),
                        uint256(),
                    )
                )
            }

            val results = blockchainGateway.readMultiCall(
                multicalls
            )
            return results.map { retVal ->
                PancakePoolInfo(
                    retVal.data[1].value as String,
                    retVal.data[2].value as String,
                    retVal.data[3].value as String,
                    retVal.data[5].value as BigInteger,
                )
            }
        }
    }



    //todo: arrowkt
    override suspend fun produceMarkets(): Flow<FarmingMarket> = channelFlow {
        val contract = getContract()

        contract.customPoolInfos().forEachIndexed { poolIndex, poolInfo ->
            launch {
                throttled {
                    toStakingMarketElement(poolInfo, contract, poolIndex)?.let {
                        send(it)
                    }
                }
            }
        }
    }

    private suspend fun toStakingMarketElement(
        poolInfo: PancakePoolInfo,
        chef: MasterChefBasedContract,
        poolId: Int
    ): FarmingMarket? {
        return try {
            val stakedtoken = getToken(poolInfo.v3Pool)
            val rewardToken = getToken(chef.rewardToken.await())
            create(
                identifier = "${chef.address}-${poolId}",
                name = stakedtoken.name + " Farm",
                stakedToken = stakedtoken,
                rewardTokens = listOf(rewardToken),
                marketSize = Refreshable.refreshable {
                    getMarketSize(stakedtoken, chef.address)
                }
            )
        } catch (ex: Exception) {
            logger.error("Error while fetching market for poolId $poolId", ex)
            null
        }
    }

    override fun getProtocol(): Protocol {
        return Protocol.PANCAKESWAP
    }

    override fun getNetwork(): Network {
        return Network.POLYGON_ZKEVM
    }

    data class PancakePoolInfo(
        val v3Pool: String,
        val token0: String,
        val token1: String,
        val totalLiquidity: BigInteger
    )
}