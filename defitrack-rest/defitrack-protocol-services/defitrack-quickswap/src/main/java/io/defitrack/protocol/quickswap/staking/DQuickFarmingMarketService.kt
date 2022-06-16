package io.defitrack.protocol.quickswap.staking

import io.defitrack.abi.ABIResource
import io.defitrack.common.network.Network
import io.defitrack.evm.contract.BlockchainGatewayProvider
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.quickswap.QuickswapService
import io.defitrack.protocol.quickswap.contract.DQuickContract
import io.defitrack.market.farming.FarmingMarketService
import io.defitrack.market.farming.domain.FarmingPositionFetcher
import io.defitrack.market.farming.domain.FarmingMarket
import io.defitrack.token.ERC20Resource
import org.springframework.stereotype.Service

@Service
class DQuickFarmingMarketService(
    private val quickswapService: QuickswapService,
    private val blockchainGatewayProvider: BlockchainGatewayProvider,
    private val abiResource: ABIResource,
    private val erC20Resource: ERC20Resource,
) : FarmingMarketService() {

    val dquickStakingABI by lazy {
        abiResource.getABI("quickswap/dquick.json")
    }

    val dquick = dquickContract()

    override suspend fun fetchStakingMarkets(): List<FarmingMarket> {

        val stakedToken = erC20Resource.getTokenInformation(getNetwork(), dquick.address).toFungibleToken()
        val quickToken =
            erC20Resource.getTokenInformation(getNetwork(), "0x831753dd7087cac61ab5644b308642cc1c33dc13")
                .toFungibleToken()

        return listOf(
            FarmingMarket(
                id = "polygon-dquick-${dquick.address.lowercase()}",
                network = getNetwork(),
                protocol = getProtocol(),
                name = "Dragon's Lair",
                stakedToken = quickToken,
                rewardTokens = listOf(
                    stakedToken
                ),
                contractAddress = dquick.address,
                vaultType = "quickswap-dquick",
                balanceFetcher = FarmingPositionFetcher(
                    stakedToken.address,
                    { user -> dquick.balanceOfMethod(user) }
                )
            )
        )
    }

    private fun dquickContract() = DQuickContract(
        blockchainGatewayProvider.getGateway(getNetwork()),
        dquickStakingABI,
        quickswapService.getDQuickContract(),
    )

    override fun getProtocol(): Protocol {
        return Protocol.QUICKSWAP
    }

    override fun getNetwork(): Network {
        return Network.POLYGON
    }
}