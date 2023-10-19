package io.defitrack.market.farming

import io.defitrack.claimable.domain.ClaimableRewardFetcher
import io.defitrack.claimable.domain.Reward
import io.defitrack.evm.contract.FarmingContract
import io.defitrack.market.farming.domain.FarmingMarket
import io.defitrack.transaction.PreparedTransaction.Companion.selfExecutingTransaction

abstract class SingleContractFarmingMarketProvider : FarmingMarketProvider() {

    override suspend fun fetchMarkets(): List<FarmingMarket> {
        val config = single()
        val stakedToken = getToken(config.contract.stakedToken.await()).toFungibleToken()
        val rewardToken = getToken(config.contract.rewardToken.await()).toFungibleToken()

        return listOf(
            create(
                name = config.name,
                identifier = config.contract.address,
                stakedToken = stakedToken,
                rewardTokens = listOf(
                    rewardToken
                ),
                claimableRewardFetcher = ClaimableRewardFetcher(
                    Reward(
                        token = rewardToken,
                        contractAddress = config.contract.address,
                        getRewardFunction = config.contract::getRewardFn
                    ),
                    preparedTransaction = selfExecutingTransaction(config.contract::claimFn)
                )
            )
        )
    }

    abstract suspend fun single(): SingleFarmingConfig

    data class SingleFarmingConfig(
        val name: String,
        val contract: FarmingContract
    )
}