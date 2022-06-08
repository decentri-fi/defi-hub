package io.defitrack.protocol.ribbon

import io.defitrack.abi.ABIResource
import io.defitrack.common.network.Network
import io.defitrack.common.utils.FormatUtilsExtensions.asEth
import io.defitrack.evm.contract.ContractAccessorGateway
import io.defitrack.price.PriceRequest
import io.defitrack.price.PriceResource
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.ribbon.contract.RibbonVaultContract
import io.defitrack.staking.StakingMarketService
import io.defitrack.staking.domain.StakingMarket
import io.defitrack.staking.domain.StakingMarketBalanceFetcher
import io.defitrack.token.ERC20Resource
import org.springframework.stereotype.Component

@Component
class RibbonAvalancheStakingMarketProvider(
    private val ribbonGraphProvider: RibbonAvalancheGraphProvider,
    private val erC20Resource: ERC20Resource,
    private val priceResource: PriceResource,
    abiResource: ABIResource,
    private val contractAccessorGateway: ContractAccessorGateway
) : StakingMarketService() {

    val ribbonVaultAbi = abiResource.getABI("ribbon/vault.json")

    override suspend fun fetchStakingMarkets(): List<StakingMarket> {
        return ribbonGraphProvider.getVaults().map {
            val stakedToken = erC20Resource.getTokenInformation(getNetwork(), it.underlyingAsset)
            val vault = RibbonVaultContract(
                contractAccessorGateway.getGateway(getNetwork()),
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
                balanceFetcher = StakingMarketBalanceFetcher(
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