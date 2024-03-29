package io.defitrack.protocol.application.sushiswap.staking

import arrow.fx.coroutines.parMapNotNull
import io.defitrack.architecture.conditional.ConditionalOnCompany
import io.defitrack.claim.ClaimableRewardFetcher
import io.defitrack.claim.Reward
import io.defitrack.common.network.Network
import io.defitrack.common.utils.refreshable
import io.defitrack.evm.position.PositionFetcher
import io.defitrack.market.domain.farming.FarmingMarket
import io.defitrack.market.port.out.FarmingMarketProvider
import io.defitrack.protocol.Company
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.sushiswap.contract.MasterchefV2Contract
import io.defitrack.transaction.PreparedTransaction.Companion.selfExecutingTransaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
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
                stakedToken = stakedtoken,
                rewardTokens = listOf(
                    rewardToken,
                    extraReward
                ).filterNotNull(),
                marketSize = refreshable {
                    getMarketSize(stakedtoken, chef.address)
                },
                claimableRewardFetcher = ClaimableRewardFetcher(
                    listOf(
                        Reward(
                            token = rewardToken,
                            getRewardFunction = chef.pendingFunction(poolId)
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
                type =  "sushiswap.masterchef.v2"
            )
        } catch (ex: Exception) {
            logger.error("Error while fetching market for poolId $poolId")
            null
        }
    }
}