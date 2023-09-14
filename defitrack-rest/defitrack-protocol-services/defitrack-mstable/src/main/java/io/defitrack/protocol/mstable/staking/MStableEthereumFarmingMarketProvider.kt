package io.defitrack.protocol.mstable.staking

import io.defitrack.common.network.Network
import io.defitrack.market.farming.FarmingMarketProvider
import io.defitrack.market.farming.domain.FarmingMarket
import io.defitrack.market.lending.domain.PositionFetcher
import io.defitrack.protocol.ContractType
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.mstable.MStableEthereumService
import io.defitrack.protocol.mstable.contract.MStableEthereumBoostedSavingsVaultContract
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.springframework.stereotype.Service

@Service
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
            vaultType = "mstable-boosted-savings-vault",
            balanceFetcher = PositionFetcher(
                address = contract.address,
                { user -> contract.rawBalanceOfFunction(user) }
            ),
            farmType = ContractType.VAULT
        )
    }

    override fun getNetwork(): Network {
        return Network.ETHEREUM
    }
}