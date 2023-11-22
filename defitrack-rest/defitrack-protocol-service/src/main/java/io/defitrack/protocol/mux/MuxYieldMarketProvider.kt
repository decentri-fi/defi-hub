package io.defitrack.protocol.mux

import io.defitrack.claimable.domain.ClaimableRewardFetcher
import io.defitrack.claimable.domain.Reward
import io.defitrack.common.network.Network
import io.defitrack.common.utils.AsyncUtils.lazyAsync
import io.defitrack.conditional.ConditionalOnCompany
import io.defitrack.market.farming.FarmingMarketProvider
import io.defitrack.market.farming.domain.FarmingMarket
import io.defitrack.protocol.Company
import io.defitrack.protocol.Protocol
import io.defitrack.transaction.PreparedTransaction.Companion.selfExecutingTransaction
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.springframework.stereotype.Component

@Component
@ConditionalOnCompany(Company.MUX)
class MuxYieldMarketProvider : FarmingMarketProvider() {

    private val yieldContractAddress = "0xaf9c4f6a0ceb02d4217ff73f3c95bbc8c7320cee"

    override suspend fun fetchMarkets(): List<FarmingMarket> = coroutineScope {
        val contract = MuxYieldContract(
            getBlockchainGateway(),
            yieldContractAddress
        )

        listOf(
            async { vestedMlp(contract) },
            async { vestedMux(contract) }
        ).awaitAll()
    }

    suspend fun vestedMlp(contract: MuxYieldContract): FarmingMarket {
        val stakedToken = getToken(contract.mlp.await()).toFungibleToken()
        val rewardToken = getToken(contract.mlp.await()).toFungibleToken()
        return create(
            name = "Vested MLP",
            identifier = contract.address + "-0",
            stakedToken = stakedToken,
            rewardTokens = listOf(rewardToken),
            claimableRewardFetcher = ClaimableRewardFetcher(
                Reward(
                    rewardToken,
                    contract.address,
                    contract::claimableVestedTokenFromMlp
                ),
                preparedTransaction = selfExecutingTransaction(contract::claimFromMlpUnwrap)
            )
        )
    }

    suspend fun vestedMux(contract: MuxYieldContract): FarmingMarket {
        val stakedToken = getToken(contract.mlp.await()).toFungibleToken()
        val rewardToken = getToken(contract.mlp.await()).toFungibleToken()
        return create(
            name = "Vested MUX",
            identifier = contract.address + "-1",
            stakedToken = stakedToken,
            rewardTokens = listOf(rewardToken),
            claimableRewardFetcher = ClaimableRewardFetcher(
                Reward(
                    rewardToken,
                    contract.address,
                    contract::claimableVestedTokenFromMux
                ),
                preparedTransaction = selfExecutingTransaction(contract::claimFromVeUnwrap)
            )
        )
    }

    override fun getProtocol(): Protocol = Protocol.MUX
    override fun getNetwork() = Network.ARBITRUM
}