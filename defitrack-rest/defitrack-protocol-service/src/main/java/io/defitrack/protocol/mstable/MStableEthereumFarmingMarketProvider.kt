package io.defitrack.protocol.mstable

import arrow.core.nel
import io.defitrack.common.network.Network
import io.defitrack.conditional.ConditionalOnCompany
import io.defitrack.market.farming.FarmingMarketProvider
import io.defitrack.market.farming.domain.FarmingMarket
import io.defitrack.market.position.PositionFetcher
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
        val mstableSavingsVault = getToken(contract.address)

        return create(
            identifier = contract.address,
            name = mstableSavingsVault.name,
            stakedToken = stakingToken,
            rewardToken = rewardsToken,
            positionFetcher = PositionFetcher(
                address = contract.address,
                contract::rawBalanceOfFunction
            ),
        )
    }

    override fun getNetwork(): Network {
        return Network.ETHEREUM
    }
}