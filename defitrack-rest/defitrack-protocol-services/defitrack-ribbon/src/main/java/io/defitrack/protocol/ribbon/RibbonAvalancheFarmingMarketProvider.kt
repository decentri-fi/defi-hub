package io.defitrack.protocol.ribbon

import io.defitrack.common.network.Network
import io.defitrack.common.utils.FormatUtilsExtensions.asEth
import io.defitrack.market.farming.FarmingMarketProvider
import io.defitrack.market.farming.domain.FarmingMarket
import io.defitrack.market.lending.domain.PositionFetcher
import io.defitrack.price.PriceRequest
import io.defitrack.protocol.ContractType
import io.defitrack.protocol.ribbon.contract.RibbonVaultContract
import kotlinx.coroutines.runBlocking
import org.springframework.stereotype.Component

@Component
class RibbonAvalancheFarmingMarketProvider(
    private val ribbonGraphProvider: RibbonAvalancheGraphProvider,
) : FarmingMarketProvider() {

    val ribbonVaultAbi by lazy {
        runBlocking {
            getAbi("ribbon/vault.json")
        }
    }

    override suspend fun fetchMarkets(): List<FarmingMarket> {
        return ribbonGraphProvider.getVaults().map {
            val stakedToken = getToken(it.underlyingAsset)
            val vault = RibbonVaultContract(
                getBlockchainGateway(),
                ribbonVaultAbi,
                it.id
            )
            create(
                identifier = it.id,
                name = it.name,
                stakedToken = stakedToken.toFungibleToken(),
                rewardTokens = listOf(stakedToken.toFungibleToken()),
                vaultType = "ribbon-vault",
                marketSize = getPriceResource().calculatePrice(
                    PriceRequest(
                        it.underlyingAsset,
                        getNetwork(),
                        it.totalBalance.asEth(stakedToken.decimals),
                        stakedToken.type
                    )
                ).toBigDecimal(),
                balanceFetcher = PositionFetcher(
                    it.id,
                    { user -> vault.balanceOfMethod(user) }
                ),
                farmType = ContractType.VAULT
            )
        }
    }

    override fun getNetwork(): Network {
        return Network.AVALANCHE
    }
}