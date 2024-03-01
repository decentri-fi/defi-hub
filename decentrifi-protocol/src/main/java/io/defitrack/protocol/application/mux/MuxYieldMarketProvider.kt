package io.defitrack.protocol.application.mux

import io.defitrack.claim.ClaimableRewardFetcher
import io.defitrack.claim.Reward
import io.defitrack.common.network.Network
import io.defitrack.architecture.conditional.ConditionalOnCompany
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.market.port.out.FarmingMarketProvider
import io.defitrack.market.domain.farming.FarmingMarket
import io.defitrack.protocol.Company
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.mux.MuxYieldContract
import io.defitrack.transaction.PreparedTransaction.Companion.selfExecutingTransaction
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.springframework.stereotype.Component

@Component
@ConditionalOnCompany(Company.MUX)
class MuxYieldMarketProvider : FarmingMarketProvider() {

    private val yieldContractAddress = "0xaf9c4f6a0ceb02d4217ff73f3c95bbc8c7320cee"

    context(BlockchainGateway)
    override suspend fun fetchMarkets(): List<FarmingMarket> = coroutineScope {
        val contract = MuxYieldContract(yieldContractAddress)

        listOf(
            async { vestedMlp(contract) },
            async { vestedMux(contract) }
        ).awaitAll()
    }

    suspend fun vestedMlp(contract: MuxYieldContract): FarmingMarket {
        val stakedToken = getToken(contract.mlp.await())
        val rewardToken = getToken(contract.mcb.await())
        return create(
            name = "Vested MLP",
            identifier = contract.address + "-0",
            stakedToken = stakedToken,
            rewardTokens = listOf(rewardToken),
            type = "mux.vested-mlp",
            claimableRewardFetcher = ClaimableRewardFetcher(
                Reward(
                    rewardToken,
                    contract::claimableVestedTokenFromMlp
                ),
                preparedTransaction = selfExecutingTransaction(contract::claimFromMlpUnwrap)
            )
        )
    }

    suspend fun vestedMux(contract: MuxYieldContract): FarmingMarket {
        val stakedToken = getToken(contract.mlp.await())
        val rewardToken = getToken(contract.mlp.await())
        return create(
            name = "Vested MUX",
            identifier = contract.address + "-1",
            stakedToken = stakedToken,
            rewardTokens = listOf(rewardToken),
            type = "mux.vested-mux",
            claimableRewardFetcher = ClaimableRewardFetcher(
                Reward(
                    rewardToken,
                    contract::claimableVestedTokenFromMux
                ),
                preparedTransaction = selfExecutingTransaction(contract::claimFromVeUnwrap)
            )
        )
    }

    override fun getProtocol(): Protocol = Protocol.MUX
    override fun getNetwork() = Network.ARBITRUM
}