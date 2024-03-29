package io.defitrack.protocol.application.pika

import io.defitrack.claim.ClaimableRewardFetcher
import io.defitrack.claim.Reward
import io.defitrack.common.network.Network
import io.defitrack.architecture.conditional.ConditionalOnCompany
import io.defitrack.market.port.out.FarmingMarketProvider
import io.defitrack.market.domain.farming.FarmingMarket
import io.defitrack.protocol.Company
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.pika.PikaStakingContract
import io.defitrack.transaction.PreparedTransaction.Companion.selfExecutingTransaction
import org.springframework.stereotype.Component
import java.math.BigInteger

@Component
@ConditionalOnCompany(Company.PIKA)
class PikaFarmingMarketProvider : FarmingMarketProvider() {

    val pikaStaking = "0x323c8b8306d8d10d7fb78151b6d4be6f160f240a"

    override suspend fun fetchMarkets(): List<FarmingMarket> {
        val contract = with(getBlockchainGateway()) { PikaStakingContract(pikaStaking) }

        val rewardPools = contract.rewardPools()
        val stakingToken = getToken(contract.stakingToken.await())
        val rewardTokens = rewardPools.map { getToken(it.getRewardToken()) }

        return listOf(
            create(
                name = "Pika Staking",
                identifier = pikaStaking,
                stakedToken = stakingToken,
                rewardTokens = rewardTokens,
                positionFetcher = defaultPositionFetcher(pikaStaking),
                internalMetadata = mapOf(
                    "contract" to contract
                ),
                type = "pika.staking",
                claimableRewardFetchers = rewardTokens.mapIndexed { index, rewardToken ->
                    val reward = rewardPools[index]
                    ClaimableRewardFetcher(
                        Reward(
                            rewardToken,
                            reward::getClaimableReward
                        ) { result, _ ->
                            when (reward) {
                                is PikaStakingContract.PikaRewardPoolContract -> {
                                    val precision = reward.getPrecision()
                                    (result[0].value as BigInteger) / precision
                                }

                                else -> {
                                    result[0].value as BigInteger
                                }
                            }
                        },
                        selfExecutingTransaction(reward::claimRewardFn)
                    )
                }
            )
        )
    }

    override fun getProtocol(): Protocol {
        return Protocol.PIKA
    }

    override fun getNetwork(): Network {
        return Network.OPTIMISM
    }
}