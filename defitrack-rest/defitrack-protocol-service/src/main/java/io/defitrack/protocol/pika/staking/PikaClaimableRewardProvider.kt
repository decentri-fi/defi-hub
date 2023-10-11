package io.defitrack.protocol.pika.staking

import io.defitrack.claimable.ClaimableMarketProvider
import io.defitrack.claimable.domain.ClaimableRewardFetcher
import io.defitrack.claimable.domain.ClaimableMarket
import io.defitrack.claimable.domain.Reward
import io.defitrack.conditional.ConditionalOnCompany
import io.defitrack.protocol.Company
import io.defitrack.protocol.pika.PikaStakingContract
import io.defitrack.transaction.PreparedTransaction.Companion.selfExecutingTransaction
import org.springframework.stereotype.Component
import java.math.BigInteger

@Component
@ConditionalOnCompany(Company.PIKA)
class PikaClaimableRewardProvider(private val pikaFarmingMarketProvider: PikaFarmingMarketProvider):
    ClaimableMarketProvider() {
    override suspend fun fetchClaimables(): List<ClaimableMarket> {
        return pikaFarmingMarketProvider.getMarkets().flatMap {
            val contract = it.internalMetadata["contract"] as PikaStakingContract

            contract.rewardPools().map { rewardPoolContract ->
                val rewardToken = pikaFarmingMarketProvider.getToken(rewardPoolContract.getRewardToken())

                ClaimableMarket(
                    name = "${rewardToken.name} staking reward",
                    network = pikaFarmingMarketProvider.getNetwork(),
                    protocol = pikaFarmingMarketProvider.getProtocol(),
                    id = "rwrd_" + rewardPoolContract.fetchAddress(),
                    claimableRewardFetcher = ClaimableRewardFetcher(
                        Reward(
                            rewardToken.toFungibleToken(),
                            rewardPoolContract.fetchAddress(),
                            rewardPoolContract::getClaimableReward,
                            extractAmountFromRewardFunction = { result, _ ->
                                when (rewardPoolContract) {
                                    is PikaStakingContract.PikaRewardPoolContract -> {
                                        val precision = rewardPoolContract.getPrecision()
                                        (result[0].value as BigInteger) / precision
                                    }
                                    else -> {
                                        result[0].value as BigInteger
                                    }
                                }
                            }
                        ),
                        selfExecutingTransaction(rewardPoolContract::claimRewardFn)
                    )
                )
            }
        }
    }
}