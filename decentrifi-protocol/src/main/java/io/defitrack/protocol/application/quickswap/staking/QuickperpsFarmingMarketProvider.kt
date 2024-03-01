package io.defitrack.protocol.quickswap.staking

import arrow.core.nel
import io.defitrack.architecture.conditional.ConditionalOnCompany
import io.defitrack.architecture.conditional.ConditionalOnNetwork
import io.defitrack.claim.ClaimableRewardFetcher
import io.defitrack.claim.Reward
import io.defitrack.common.network.Network
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.market.domain.farming.FarmingMarket
import io.defitrack.market.port.out.FarmingMarketProvider
import io.defitrack.protocol.Company
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.quickswap.contract.FQLPContract
import io.defitrack.transaction.PreparedTransaction.Companion.selfExecutingTransaction
import org.springframework.stereotype.Component

@ConditionalOnCompany(Company.QUICKSWAP)
@ConditionalOnNetwork(Network.POLYGON_ZKEVM)
@Component
class QuickperpsFarmingMarketProvider : FarmingMarketProvider() {

    val feeQlpTrackerAddress = "0xd3ee28cb8ed02a5641dfa02624df399b01f1e131"
    val depositToken = "0xc8e48fd037d1c4232f294b635e74d33a0573265a"

    context(BlockchainGateway)
    override suspend fun fetchMarkets(): List<FarmingMarket> {
        val contract = FQLPContract(feeQlpTrackerAddress)
        val rewardTokens = contract.getAllRewardTokens().map {
            getToken(it)
        }

        return create(
            name = "Fee QLP Tracker",
            identifier = feeQlpTrackerAddress,
            stakedToken = getToken(depositToken),
            rewardTokens = rewardTokens,
            type = "quickswap.quickperp-farming",
            claimableRewardFetcher = ClaimableRewardFetcher(
                rewards = rewardTokens.map {
                    Reward(
                        token = it,
                        getRewardFunction = contract.claimableReward(it.address)
                    )
                }, preparedTransaction = selfExecutingTransaction(contract::claimAllFn)
            )
        ).nel()
    }

    override fun getProtocol(): Protocol {
        return Protocol.QUICKSWAP
    }

    override fun getNetwork(): Network {
        return Network.POLYGON_ZKEVM
    }
}