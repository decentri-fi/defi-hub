package io.defitrack.protocol.ribbon

import io.defitrack.abi.ABIResource
import io.defitrack.common.network.Network
import io.defitrack.common.utils.FormatUtilsExtensions.asEth
import io.defitrack.evm.contract.BlockchainGatewayProvider
import io.defitrack.price.PriceRequest
import io.defitrack.price.PriceResource
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.ribbon.contract.RibbonVaultContract
import io.defitrack.market.farming.FarmingMarketProvider
import io.defitrack.market.farming.domain.FarmingMarket
import io.defitrack.market.farming.domain.FarmingPositionFetcher
import io.defitrack.token.ERC20Resource
import org.springframework.stereotype.Component

@Component
class RibbonAvalancheFarmingMarketProvider(
    private val ribbonGraphProvider: RibbonAvalancheGraphProvider,
    private val erC20Resource: ERC20Resource,
    private val priceResource: PriceResource,
    abiResource: ABIResource,
    private val blockchainGatewayProvider: BlockchainGatewayProvider
) : FarmingMarketProvider() {

    val ribbonVaultAbi = abiResource.getABI("ribbon/vault.json")

    override suspend fun fetchStakingMarkets(): List<FarmingMarket> {
        return ribbonGraphProvider.getVaults().map {
            val stakedToken = erC20Resource.getTokenInformation(getNetwork(), it.underlyingAsset)
            val vault = RibbonVaultContract(
                blockchainGatewayProvider.getGateway(getNetwork()),
                ribbonVaultAbi,
                it.id
            )
            stakingMarket(
                id = "ribbon-avax-${it.id}",
                name = it.name,
                stakedToken = stakedToken.toFungibleToken(),
                rewardTokens = listOf(stakedToken.toFungibleToken()),
                contractAddress = it.id,
                vaultType = "ribbon-vault",
                marketSize = priceResource.calculatePrice(
                    PriceRequest(
                        it.underlyingAsset,
                        getNetwork(),
                        it.totalBalance.asEth(stakedToken.decimals),
                        stakedToken.type
                    )
                ).toBigDecimal(),
                balanceFetcher = FarmingPositionFetcher(
                    it.id,
                    { user -> vault.balanceOfMethod(user) }
                )
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