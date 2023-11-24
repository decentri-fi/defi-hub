package io.defitrack.protocol.sushiswap.staking

import arrow.fx.coroutines.parMapNotNull
import io.defitrack.claimable.domain.ClaimableRewardFetcher
import io.defitrack.claimable.domain.Reward
import io.defitrack.common.network.Network
import io.defitrack.common.utils.AsyncUtils
import io.defitrack.common.utils.AsyncUtils.lazyAsync
import io.defitrack.common.utils.Refreshable
import io.defitrack.conditional.ConditionalOnCompany
import io.defitrack.market.farming.FarmingMarketProvider
import io.defitrack.market.farming.domain.FarmingMarket
import io.defitrack.market.position.PositionFetcher
import io.defitrack.network.toVO
import io.defitrack.protocol.Company
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.sushiswap.contract.MasterchefV2Contract
import io.defitrack.transaction.PreparedTransaction
import io.defitrack.transaction.PreparedTransaction.Companion.selfExecutingTransaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.launch
import org.springframework.stereotype.Component

@Component
@ConditionalOnCompany(Company.SUSHISWAP)
class SushiswapEthereumMasterchefV2MarketProvider : FarmingMarketProvider() {

    private val masterchefV2ContractAddress = "0xef0881ec094552b2e128cf945ef17a6752b4ec5d"

    override suspend fun produceMarkets(): Flow<FarmingMarket> = channelFlow {
        val contract = MasterchefV2Contract(
            getBlockchainGateway(),
            masterchefV2ContractAddress
        )

        (0 until contract.poolInfos().size).parMapNotNull { index ->
            toStakingMarketElement(contract, index)
        }.forEach {
            send(it)
        }
    }

    override fun getProtocol(): Protocol {
        return Protocol.SUSHISWAP
    }

    override fun getNetwork(): Network {
        return Network.ETHEREUM
    }

    private suspend fun toStakingMarketElement(
        chef: MasterchefV2Contract,
        poolId: Int
    ): FarmingMarket? {
        return try {
            val stakedtoken = getToken(chef.lpToken(poolId))
            val rewarder = chef.rewarder(poolId)
            val rewarderContract = when (rewarder != "0x0000000000000000000000000000000000000000") {
                true -> MasterchefV2Contract.Rewarder(getBlockchainGateway(), rewarder)
                false -> null
            }

            val rewardToken = getToken(chef.rewardToken.await())
            val extraReward = when (rewarderContract != null) {
                true -> getToken(rewarderContract.rewardToken.await())
                false -> null
            }

            create(
                identifier = "${chef.address}-${poolId}",
                name = stakedtoken.name + " Farm",
                stakedToken = stakedtoken.toFungibleToken(),
                rewardTokens = listOf(
                    rewardToken.toFungibleToken(),
                    extraReward?.toFungibleToken()
                ).filterNotNull(),
                marketSize = Refreshable.refreshable {
                    getMarketSize(stakedtoken.toFungibleToken(), chef.address)
                },
                claimableRewardFetcher = ClaimableRewardFetcher(
                    listOf(
                        Reward(
                            token = rewardToken.toFungibleToken(),
                            getRewardFunction = { user ->
                                chef.pendingFunction(poolId, user)
                            }
                        ),
                        rewarderContract?.let { rctr ->
                            Reward(
                                token = extraReward!!,
                                { user -> rctr.pendingTokenFn(poolId, user) }
                            )
                        }
                    ).filterNotNull(),
                    preparedTransaction = selfExecutingTransaction(chef.harvestFunction(poolId))
                ),
                positionFetcher = PositionFetcher(
                    { user -> chef.userInfoFunction(poolId, user) }
                ),
            )
        } catch (ex: Exception) {
            logger.error("Error while fetching market for poolId $poolId")
            null
        }
    }
}