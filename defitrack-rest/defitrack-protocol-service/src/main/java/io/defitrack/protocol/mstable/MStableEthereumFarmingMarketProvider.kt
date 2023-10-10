package io.defitrack.protocol.mstable

import io.defitrack.common.network.Network
import io.defitrack.conditional.ConditionalOnCompany
import io.defitrack.market.farming.FarmingMarketProvider
import io.defitrack.market.farming.domain.FarmingMarket
import io.defitrack.market.lending.domain.PositionFetcher
import io.defitrack.protocol.Company
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.mstable.contract.MStableEthereumBoostedSavingsVaultContract
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.springframework.stereotype.Component

@Component
@ConditionalOnCompany(Company.MSTABLE)
class MStableEthereumFarmingMarketProvider(
    private val mStableEthereumService: MStableEthereumService,
) : FarmingMarketProvider() {

    override suspend fun fetchMarkets(): List<FarmingMarket> = coroutineScope {
        mStableEthereumService.getBoostedSavingsVaults().map {
            MStableEthereumBoostedSavingsVaultContract(
                getBlockchainGateway(),
                it
            )
        }.map { contract ->
            async {
                try {
                    toStakingMarket(contract)
                } catch (ex: Exception) {
                    ex.printStackTrace()
                    null
                }
            }
        }.awaitAll().filterNotNull()
    }

    override fun getProtocol(): Protocol {
        return Protocol.MSTABLE
    }

    private suspend fun toStakingMarket(contract: MStableEthereumBoostedSavingsVaultContract): FarmingMarket {
        val stakingToken = getToken(contract.stakingToken())
        val rewardsToken = getToken(contract.rewardsToken())
        return create(
            identifier = contract.address,
            name = contract.name(),
            stakedToken = stakingToken.toFungibleToken(),
            rewardTokens = listOf(
                rewardsToken.toFungibleToken()
            ),
            balanceFetcher = PositionFetcher(
                address = contract.address,
                { user -> contract.rawBalanceOfFunction(user) }
            ),
        )
    }

    override fun getNetwork(): Network {
        return Network.ETHEREUM
    }
}