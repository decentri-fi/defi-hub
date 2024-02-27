package io.defitrack.protocol.application.ethos

import arrow.core.nel
import io.defitrack.claim.ClaimableRewardFetcher
import io.defitrack.claim.Reward
import io.defitrack.common.network.Network
import io.defitrack.architecture.conditional.ConditionalOnCompany
import io.defitrack.market.port.out.FarmingMarketProvider
import io.defitrack.market.domain.farming.FarmingMarket
import io.defitrack.evm.position.PositionFetcher
import io.defitrack.protocol.Company
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.ethos.EthosStabilityPool
import io.defitrack.transaction.PreparedTransaction.Companion.selfExecutingTransaction
import org.springframework.stereotype.Component

@Component
@ConditionalOnCompany(Company.ETHOS)
class EthosFarmingProvider : FarmingMarketProvider() {

    val oath = "0x39fde572a18448f8139b7788099f0a0740f51205"
    val stabilityPoolAddress = "0x8b147a2d4fc3598079c64b8bf9ad2f776786cfed"

    override suspend fun fetchMarkets(): List<FarmingMarket> {
        val contract = EthosStabilityPool(
            getBlockchainGateway(), stabilityPoolAddress
        )

        val reward = getToken(oath)
        val staked = getToken(contract.ernToken.await())

        return create(
            name = "Ethos Farming",
            identifier = stabilityPoolAddress,
            stakedToken = staked,
            rewardToken = reward,
            positionFetcher = PositionFetcher(contract::depositsFn),
            claimableRewardFetcher = ClaimableRewardFetcher(
                Reward(
                    reward,
                    contract::claimableFn,
                ),
                preparedTransaction = selfExecutingTransaction(contract::claimFn)
            )
        ).nel()
    }

    override fun getProtocol(): Protocol {
        return Protocol.ETHOS
    }

    override fun getNetwork(): Network {
        return Network.OPTIMISM
    }
}