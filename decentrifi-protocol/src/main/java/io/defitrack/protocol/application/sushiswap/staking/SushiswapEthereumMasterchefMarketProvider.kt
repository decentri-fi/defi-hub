package io.defitrack.protocol.application.sushiswap.staking

import io.defitrack.claim.ClaimableRewardFetcher
import io.defitrack.claim.Reward
import io.defitrack.common.network.Network
import io.defitrack.common.utils.AsyncUtils.lazyAsync
import io.defitrack.common.utils.refreshable
import io.defitrack.architecture.conditional.ConditionalOnCompany
import io.defitrack.market.port.out.FarmingMarketProvider
import io.defitrack.market.domain.farming.FarmingMarket
import io.defitrack.evm.position.PositionFetcher
import io.defitrack.protocol.Company
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.sushiswap.contract.MasterChefBasedContract
import io.defitrack.protocol.sushiswap.contract.MasterChefPoolInfo
import io.defitrack.transaction.PreparedTransaction.Companion.selfExecutingTransaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.launch
import org.springframework.stereotype.Component


@Component
@ConditionalOnCompany(Company.SUSHISWAP)
class SushiswapEthereumMasterchefMarketProvider : FarmingMarketProvider() {

    private val masterchefContractAddress = "0xc2EdaD668740f1aA35E4D8f227fB8E17dcA888Cd"

    val deferredContract = lazyAsync {
        MasterChefBasedContract(
            "sushi",
            "pendingSushi",
            getBlockchainGateway(),
            masterchefContractAddress
        )
    }

    override suspend fun produceMarkets(): Flow<FarmingMarket> = channelFlow {
        val contract = deferredContract.await()
        contract.defaultPoolInfos.await().forEachIndexed { poolIndex, poolInfo ->
            launch {
                throttled {
                    toStakingMarketElement(poolInfo, contract, poolIndex)?.let {
                        send(it)
                    }
                }
            }
        }
    }

    override fun getProtocol(): Protocol {
        return Protocol.SUSHISWAP
    }

    override fun getNetwork(): Network {
        return Network.ETHEREUM
    }

    private suspend fun toStakingMarketElement(
        poolInfo: MasterChefPoolInfo,
        chef: MasterChefBasedContract,
        poolId: Int
    ): FarmingMarket? {
        return try {
            val stakedtoken = getToken(poolInfo.lpToken)
            val rewardToken = getToken(chef.rewardToken.await())
            create(
                identifier = "${chef.address}-${poolId}",
                name = stakedtoken.name + " Farm",
                stakedToken = stakedtoken,
                rewardTokens = listOf(rewardToken),
                marketSize = refreshable {
                    getMarketSize(stakedtoken, chef.address)
                },
                claimableRewardFetcher = ClaimableRewardFetcher(
                    Reward(
                        token = rewardToken,
                        getRewardFunction = chef.pendingFunction(poolId)
                    ),
                    preparedTransaction = selfExecutingTransaction(chef.harvestFunction(poolId))
                ),
                positionFetcher = PositionFetcher(
                    chef.userInfoFunction(poolId)
                ),
                type = "sushiswap.masterchef"
            )
        } catch (ex: Exception) {
            logger.error("Error while fetching market for poolId $poolId", ex)
            null
        }
    }
}