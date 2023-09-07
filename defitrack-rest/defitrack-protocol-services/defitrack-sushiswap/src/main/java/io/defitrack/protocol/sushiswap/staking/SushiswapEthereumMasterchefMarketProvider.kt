package io.defitrack.protocol.sushiswap.staking

import io.defitrack.claimable.ClaimableRewardFetcher
import io.defitrack.common.network.Network
import io.defitrack.common.utils.AsyncUtils.lazyAsync
import io.defitrack.common.utils.Refreshable.Companion.refreshable
import io.defitrack.market.farming.FarmingMarketProvider
import io.defitrack.market.farming.domain.FarmingMarket
import io.defitrack.market.lending.domain.PositionFetcher
import io.defitrack.network.toVO
import io.defitrack.protocol.ContractType
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.contract.MasterChefBasedContract
import io.defitrack.protocol.contract.MasterChefPoolInfo
import io.defitrack.protocol.sushiswap.apr.MasterchefBasedfStakingAprCalculator
import io.defitrack.transaction.PreparedTransaction
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import org.springframework.stereotype.Component


//@Component
class SushiswapEthereumMasterchefMarketProvider : FarmingMarketProvider() {

    private val masterchefContractAddress = "0xc2EdaD668740f1aA35E4D8f227fB8E17dcA888Cd"

    val deferredContract = lazyAsync {
        MasterChefBasedContract(
            "sushi",
            "sushiPerBlock",
            "pendingSushi",
            getBlockchainGateway(),
            masterchefContractAddress
        )
    }

    override suspend fun produceMarkets(): Flow<FarmingMarket> = channelFlow {
        val contract = deferredContract.await()
        contract.poolInfos().mapIndexed { poolIndex, poolInfo ->
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
                stakedToken = stakedtoken.toFungibleToken(),
                rewardTokens = listOf(
                    rewardToken.toFungibleToken()
                ),
                vaultType = "sushi-masterchef",
                marketSize = refreshable {
                    getMarketSize(stakedtoken.toFungibleToken(), chef.address)
                },
                claimableRewardFetcher = ClaimableRewardFetcher(
                    address = chef.address,
                    function = { user ->
                        chef.pendingFunction(poolId, user)
                    },
                    preparedTransaction = { user ->
                        PreparedTransaction(
                            network = getNetwork().toVO(),
                            chef.harvestFunction(poolId),
                            to = chef.address,
                            from = user
                        )
                    }
                ),
                apr = MasterchefBasedfStakingAprCalculator(
                    getERC20Resource(),
                    getPriceResource(),
                    chef,
                    poolId
                ).calculateApr(),
                balanceFetcher = PositionFetcher(
                    chef.address,
                    { user -> chef.userInfoFunction(poolId, user) }
                ),
                farmType = ContractType.LIQUIDITY_MINING
            )
        } catch (ex: Exception) {
            logger.error("Error while fetching market for poolId $poolId", ex)
            null
        }
    }
}