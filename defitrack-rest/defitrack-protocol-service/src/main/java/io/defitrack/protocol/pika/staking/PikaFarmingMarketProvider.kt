package io.defitrack.protocol.pika.staking

import io.defitrack.claimable.ClaimableMarket
import io.defitrack.claimable.ClaimableRewardFetcher
import io.defitrack.claimable.Reward
import io.defitrack.common.network.Network
import io.defitrack.conditional.ConditionalOnCompany
import io.defitrack.erc20.TokenInformationVO
import io.defitrack.market.farming.FarmingMarketProvider
import io.defitrack.market.farming.domain.FarmingMarket
import io.defitrack.protocol.Company
import io.defitrack.protocol.ContractType
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.pika.PikaStakingContract
import io.defitrack.transaction.PreparedTransaction
import org.springframework.stereotype.Component
import java.math.BigInteger

@Component
@ConditionalOnCompany(Company.PIKA)
class PikaFarmingMarketProvider : FarmingMarketProvider() {

    val pikaStaking = "0x323c8b8306d8d10d7fb78151b6d4be6f160f240a"

    override suspend fun fetchMarkets(): List<FarmingMarket> {
        val contract = PikaStakingContract(
            getBlockchainGateway(), pikaStaking
        )

        val rewardPools = contract.rewardPools()
        val stakingToken = getToken(contract.stakingToken.await())
        val rewardTokens = rewardPools.map { getToken(it.getRewardToken()) }

        return listOf(
            create(
                name = "Pika Staking",
                identifier = pikaStaking,
                stakedToken = stakingToken.toFungibleToken(),
                rewardTokens = rewardTokens.map(TokenInformationVO::toFungibleToken),
                farmType = ContractType.STAKING,
                balanceFetcher = defaultPositionFetcher(pikaStaking),
                internalMetadata = mapOf(
                    "contract" to contract
                )
            )
        )
    }

    override suspend fun getClaimables(): List<ClaimableMarket> {
        return getMarkets().flatMap {
            val contract = it.internalMetadata["contract"] as PikaStakingContract

            contract.rewardPools().map { rewardPoolContract ->
                val rewardToken = getToken(rewardPoolContract.getRewardToken())

                ClaimableMarket(
                    name = "${rewardToken.name} staking reward",
                    network = getNetwork(),
                    protocol = getProtocol(),
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
                        )
                    ) {
                        PreparedTransaction(
                            rewardPoolContract.claimRewardFn(it),
                            it
                        )
                    }
                )
            }

        }
    }

    override fun getProtocol(): Protocol {
        return Protocol.PIKA
    }

    override fun getNetwork(): Network {
        return Network.OPTIMISM
    }
}