package io.defitrack.protocol.application.magpiexyz

import arrow.fx.coroutines.parMap
import io.defitrack.architecture.conditional.ConditionalOnCompany
import io.defitrack.claim.ClaimableRewardFetcher
import io.defitrack.claim.Reward
import io.defitrack.common.network.Network
import io.defitrack.evm.position.PositionFetcher
import io.defitrack.market.domain.farming.FarmingMarket
import io.defitrack.market.port.out.FarmingMarketProvider
import io.defitrack.protocol.Company
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.magpiexyz.MasterPenPieContract
import io.defitrack.transaction.PreparedTransaction.Companion.selfExecutingTransaction
import org.springframework.stereotype.Component

@Component
@ConditionalOnCompany(Company.MAGPIE_XYZ)
class MagpieMasterpenArbitrumFarmProvider : FarmingMarketProvider() {

    val magpieAddress = "0x0776c06907ce6ff3d9dbf84ba9b3422d7225942d"

    override suspend fun fetchMarkets(): List<FarmingMarket> {
        val contract = MasterPenPieContract(getBlockchainGateway(), magpieAddress)
        val poolLength = contract.poolLength.await()
        return (0 until poolLength.toInt()).parMap(concurrency = 8) { index ->
            val stakingTokenAddress = contract.registeredToken(index.toBigInteger())
            val info = contract.tokenToPoolInfo(stakingTokenAddress)

            val stakedToken = getToken(info.stakingToken)
            val token = getToken(info.receiptToken)

            val rewards = info.rewarder.rewardTokens().map {
                getToken(it)
            }

            //TODO: claimables don't work yet
            create(
                name = info.stakingToken,
                token = token,
                identifier = "$magpieAddress-$index",
                stakedToken = stakedToken,
                rewardTokens = rewards,
                positionFetcher = PositionFetcher(
                    contract.userInfo(info.stakingToken)
                ),
                claimableRewardFetcher = ClaimableRewardFetcher(
                    rewards = rewards.map {
                        Reward(
                            it,
                            info.rewarder.earnedFn(it.address)
                        )
                    },
                    selfExecutingTransaction { _ ->
                        contract.multiclaimFn(listOf(info.stakingToken))
                    }
                ),
                metadata = mapOf("rewarder" to info.rewarder)
            )
        }
    }

    override fun getProtocol(): Protocol {
        return Protocol.MAGPIE_XYZ
    }

    override fun getNetwork(): Network {
        return Network.ARBITRUM
    }
}