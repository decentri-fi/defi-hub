package io.defitrack.protocol.ribbon

import io.defitrack.abi.ABIResource
import io.defitrack.common.network.Network
import io.defitrack.common.utils.FormatUtilsExtensions.asEth
import io.defitrack.evm.contract.BlockchainGatewayProvider
import io.defitrack.price.PriceRequest
import io.defitrack.price.PriceResource
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.ribbon.contract.RibbonVaultContract
import io.defitrack.market.farming.FarmingMarketService
import io.defitrack.market.farming.domain.FarmingMarket
import io.defitrack.market.farming.domain.FarmingPositionFetcher
import io.defitrack.token.ERC20Resource
import org.springframework.stereotype.Component

@Component
class RibbonEthereumFarmingMarketProvider(
    private val ribbonEthereumGraphProvider: RibbonEthereumGraphProvider,
    private val erC20Resource: ERC20Resource,
    private val priceResource: PriceResource,
    private val abiResource: ABIResource,
    private val blockchainGatewayProvider: BlockchainGatewayProvider
) : FarmingMarketService() {

    val ribbonVaultAbi = abiResource.getABI("ribbon/vault.json")

    override suspend fun fetchStakingMarkets(): List<FarmingMarket> {
        return ribbonEthereumGraphProvider.getVaults().map {
            val stakedToken = erC20Resource.getTokenInformation(getNetwork(), it.underlyingAsset)
            val vault = RibbonVaultContract(
                blockchainGatewayProvider.getGateway(getNetwork()),
                ribbonVaultAbi,
                it.id
            )
            stakingMarket(
                id = "ribbon-ethereum-${it.id}",
                name = it.name,
                stakedToken = stakedToken.toFungibleToken(),
                rewardTokens = listOf(stakedToken.toFungibleToken()),
                contractAddress = it.id,
                vaultType = "ribbon-theta-vault",
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
        return Network.ETHEREUM
    }
}