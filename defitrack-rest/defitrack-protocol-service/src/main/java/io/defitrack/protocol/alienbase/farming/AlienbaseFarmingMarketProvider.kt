package io.defitrack.protocol.alienbase.farming

import io.defitrack.claimable.domain.ClaimableRewardFetcher
import io.defitrack.claimable.domain.Reward
import io.defitrack.common.network.Network
import io.defitrack.common.utils.AsyncUtils.lazyAsync
import io.defitrack.conditional.ConditionalOnCompany
import io.defitrack.market.farming.FarmingMarketProvider
import io.defitrack.market.farming.domain.FarmingMarket
import io.defitrack.market.position.PositionFetcher
import io.defitrack.network.toVO
import io.defitrack.protocol.Company
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.alienbase.BasedDistributorV2Contract
import io.defitrack.protocol.alienbase.ComplexRewarderPerSecV4Contract
import io.defitrack.transaction.PreparedTransaction
import io.defitrack.transaction.PreparedTransaction.Companion.selfExecutingTransaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import org.springframework.stereotype.Component
import org.web3j.abi.datatypes.Address
import org.web3j.abi.datatypes.generated.Uint256
import java.math.BigInteger

@Component
@ConditionalOnCompany(Company.ALIENBASE)
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
                val rewardToken = getToken(contract.alb())

                val poolRewarders = contract.poolRewarders(poolId)
                val extraRewards = poolRewarders.map {
                    ComplexRewarderPerSecV4Contract(
                        getBlockchainGateway(),
                        it
                    )
                }.map {
                    it.rewardToken()
                }.map {
                    getToken(it)
                }

                send(
                    create(
                        identifier = "${farmingContractAddress}-${poolId}",
                        name = stakedtoken.name + " Farm",
                        stakedToken = stakedtoken,
                        rewardToken = rewardToken,
                        positionFetcher = PositionFetcher(
                            contract.userInfoFunction(poolId)
                        ),
                        claimableRewardFetcher = ClaimableRewardFetcher(
                            listOf(
                                Reward(
                                    rewardToken,
                                    { user -> contract.pendingFunction(poolId, user) },
                                    { results, _ ->
                                        val addresses = (results[0].value as List<Address>).map { it.value as String }
                                        val amounts = (results[3].value as List<Uint256>).map { it.value as BigInteger }
                                        addresses.mapIndexed { index, s ->
                                            if (s.lowercase() == rewardToken.address.lowercase()) {
                                                amounts[index]
                                            } else {
                                                BigInteger.ZERO
                                            }
                                        }.firstOrNull() ?: BigInteger.ZERO
                                    }
                                )
                            ) + extraRewards.map { extraReward ->
                                Reward(
                                    extraReward,
                                    { user -> contract.pendingFunction(poolId, user) },
                                    { results, user ->
                                        val addresses = (results[0].value as List<Address>).map { it.value as String }
                                        val amounts = (results[3].value as List<Uint256>).map { it.value as BigInteger }
                                        addresses.mapIndexed { index, s ->
                                            if (s.lowercase() == extraReward.address.lowercase()) {
                                                amounts[index]
                                            } else {
                                                BigInteger.ZERO
                                            }
                                        }.firstOrNull {
                                            it > BigInteger.ZERO
                                        } ?: BigInteger.ZERO
                                    }
                                )
                            },
                            preparedTransaction = selfExecutingTransaction(contract.claimFunction(poolId))
                        ),
                        internalMetadata = mapOf(
                            "contract" to farmingContract.await(),
                            "poolId" to poolId
                        ),
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