package io.defitrack.protocol.radiant

import io.defitrack.claim.ClaimableRewardFetcher
import io.defitrack.claim.Reward
import io.defitrack.common.network.Network
import io.defitrack.architecture.conditional.ConditionalOnCompany
import io.defitrack.market.port.out.FarmingMarketProvider
import io.defitrack.market.domain.farming.FarmingMarket
import io.defitrack.protocol.Company
import io.defitrack.protocol.Protocol
import io.defitrack.transaction.PreparedTransaction.Companion.selfExecutingTransaction
import org.springframework.stereotype.Component
import org.web3j.abi.datatypes.DynamicArray
import java.math.BigInteger

@Component
@ConditionalOnCompany(Company.RADIANT)
class RadiantMultiFeeDistributorFarmingMarketProvider : FarmingMarketProvider() {

    val address = "0xc2054a8c33bfce28de8af4af548c48915c455c13"

    override suspend fun fetchMarkets(): List<FarmingMarket> {
        val contract = RadiantMultiFeeDistributor(
            getBlockchainGateway(),
            address
        )

        val stakingToken = getToken(contract.stakingToken.await())
        val rewardTokens = contract.rewardTokens().map { getToken(it) }

        return listOf(
            create(
                name = "Radiant Staking",
                identifier = address,
                stakedToken = stakingToken,
                rewardTokens = rewardTokens,
                claimableRewardFetcher = ClaimableRewardFetcher(
                    rewards = rewardTokens.map { token ->
                        Reward(
                            token,
                            contract::getClaimableRewardFn
                        ) { result, _ ->
                            val value =
                                (result[0] as DynamicArray<RadiantMultiFeeReward>).value as List<RadiantMultiFeeReward>
                            value.mapNotNull { reward ->

                                if (reward.amount.value > BigInteger.ZERO) {
                                    reward
                                } else null
                            }.find {
                                it.rewardAddress.value.lowercase() == token.address.lowercase()
                            }?.amount?.value ?: BigInteger.ZERO
                        }
                    },
                    preparedTransaction = selfExecutingTransaction { _ ->
                        contract.getRewardFn(
                            rewardTokens.map { it.address }
                        )
                    }
                )
            )
        )

    }

    override fun getProtocol(): Protocol {
        return Protocol.RADIANT
    }

    override fun getNetwork(): Network {
        return Network.ARBITRUM
    }
}