package io.defitrack.alienbase.farming

import io.defitrack.claimable.ClaimableRewardFetcher
import io.defitrack.common.network.Network
import io.defitrack.common.utils.AsyncUtils.lazyAsync
import io.defitrack.common.utils.Refreshable
import io.defitrack.market.farming.FarmingMarketProvider
import io.defitrack.market.farming.domain.FarmingMarket
import io.defitrack.market.lending.domain.PositionFetcher
import io.defitrack.network.toVO
import io.defitrack.protocol.BasedDistributorV2Contract
import io.defitrack.protocol.ContractType
import io.defitrack.protocol.Protocol
import io.defitrack.transaction.PreparedTransaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import org.springframework.stereotype.Component

@Component
class AlienbaseFarmingMarketProvider : FarmingMarketProvider() {

    val farmingContractAddress = "0x52eaecac2402633d98b95213d0b473e069d86590"

    val farmingContract = lazyAsync {
        BasedDistributorV2Contract(
            blockchainGateway = getBlockchainGateway(),
            contractAddress = farmingContractAddress
        )
    }

    override suspend fun produceMarkets(): Flow<FarmingMarket> {
        val contract = getFarmingContract()
        return channelFlow {

            contract.poolInfos().forEachIndexed { poolId, poolInfo ->
                val stakedtoken = getToken(poolInfo.lpToken)
                val rewardToken = getToken(contract.rewardToken())

                send(
                    create(
                        identifier = "${farmingContractAddress}-${poolId}",
                        name = stakedtoken.name + " Farm",
                        stakedToken = stakedtoken.toFungibleToken(),
                        rewardTokens = listOf(
                            rewardToken.toFungibleToken()
                        ),
                        balanceFetcher = PositionFetcher(
                            address = farmingContractAddress,
                            { user ->
                                contract.userInfoFunction(
                                    user, poolId
                                )
                            },
                        ),
                        claimableRewardFetcher = ClaimableRewardFetcher(
                            address = farmingContractAddress,
                            function = { user ->
                                contract.pendingFunction(poolId, user)
                            },
                            preparedTransaction = { user ->
                                PreparedTransaction(
                                    getNetwork().toVO(),
                                    contract.claimFunction(poolId),
                                    farmingContractAddress,
                                    user
                                )
                            }
                        ),
                        vaultType = "alienbase-reward",
                        farmType = ContractType.LIQUIDITY_MINING
                    )
                )
            }

        }
    }

    private suspend fun getFarmingContract() = farmingContract.await()

    override fun getProtocol(): Protocol {
        return Protocol.ALIENBASE
    }

    override fun getNetwork(): Network {
        return Network.BASE
    }
}