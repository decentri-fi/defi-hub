package io.defitrack.protocol.mstable

import arrow.fx.coroutines.parMapNotNull
import io.defitrack.common.network.Network
import io.defitrack.conditional.ConditionalOnCompany
import io.defitrack.market.farming.FarmingMarketProvider
import io.defitrack.market.farming.domain.FarmingMarket
import io.defitrack.market.position.PositionFetcher
import io.defitrack.protocol.Company
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.mstable.contract.MStableEthereumBoostedSavingsVaultContract
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
        }.parMapNotNull(concurrency = 12) { contract ->
            try {
                toStakingMarket(contract)
            } catch (ex: Exception) {
                ex.printStackTrace()
                null
            }
        }
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
                contract::rawBalanceOfFunction
            ),
        )
    }

    override fun getNetwork(): Network {
        return Network.ETHEREUM
    }
}