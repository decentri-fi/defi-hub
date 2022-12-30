package io.defitrack.protocol.ribbon

import io.defitrack.abi.ABIResource
import io.defitrack.common.network.Network
import io.defitrack.common.utils.FormatUtilsExtensions.asEth
import io.defitrack.evm.contract.BlockchainGatewayProvider
import io.defitrack.market.farming.FarmingMarketProvider
import io.defitrack.market.farming.domain.FarmingMarket
import io.defitrack.market.lending.domain.PositionFetcher
import io.defitrack.price.PriceRequest
import io.defitrack.price.PriceResource
import io.defitrack.protocol.FarmType
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.ribbon.contract.RibbonVaultContract
import io.defitrack.token.ERC20Resource
import org.springframework.stereotype.Component

@Component
class RibbonAvalancheFarmingMarketProvider(
    private val ribbonGraphProvider: RibbonAvalancheGraphProvider,
    private val priceResource: PriceResource,
    abiResource: ABIResource,
) : FarmingMarketProvider() {

    val ribbonVaultAbi = abiResource.getABI("ribbon/vault.json")

    override suspend fun fetchMarkets(): List<FarmingMarket> {
        return ribbonGraphProvider.getVaults().map {
            val stakedToken = erC20Resource.getTokenInformation(getNetwork(), it.underlyingAsset)
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
                marketSize = priceResource.calculatePrice(
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
                farmType = FarmType.VAULT
            )
        }
    }

    override fun getProtocol(): Protocol {
        return Protocol.RIBBON
    }

    override fun getNetwork(): Network {
        return Network.AVALANCHE
    }
}