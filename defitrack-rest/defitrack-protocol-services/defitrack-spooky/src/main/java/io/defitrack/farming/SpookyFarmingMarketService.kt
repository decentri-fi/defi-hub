package io.defitrack.farming

import io.defitrack.abi.ABIResource
import io.defitrack.common.network.Network
import io.defitrack.evm.contract.BlockchainGatewayProvider
import io.defitrack.market.farming.FarmingMarketService
import io.defitrack.price.PriceResource
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.SpookyFantomService
import io.defitrack.protocol.reward.MasterchefLpContract
import io.defitrack.market.farming.domain.FarmingPositionFetcher
import io.defitrack.market.farming.domain.FarmingMarket
import io.defitrack.token.ERC20Resource
import io.defitrack.token.MarketSizeService
import org.springframework.stereotype.Component

@Component
class SpookyFarmingMarketService(
    private val spookyFantomService: SpookyFantomService,
    private val abiResource: ABIResource,
    private val erC20Resource: ERC20Resource,
    private val priceResource: PriceResource,
    private val blockchainGatewayProvider: BlockchainGatewayProvider,
    private val marketSizeService: MarketSizeService
) : FarmingMarketService() {

    override suspend fun fetchStakingMarkets(): List<FarmingMarket> {
        val masterchef = MasterchefLpContract(
            blockchainGatewayProvider.getGateway(getNetwork()),
            abiResource.getABI("spooky/Masterchef.json"),
            spookyFantomService.getMasterchef()
        )

        val reward = erC20Resource.getTokenInformation(getNetwork(), masterchef.rewardToken)

        return masterchef.poolInfos.mapIndexed { index, value ->

            val stakedToken = erC20Resource.getTokenInformation(getNetwork(), value.lpToken)
            val aprCalculator = MinichefStakingAprCalculator(
                erC20Resource,
                priceResource,
                masterchef,
                index,
                stakedToken
            )
            FarmingMarket(
                id = "fantom-spooky-${masterchef.address}-${index}",
                network = getNetwork(),
                protocol = getProtocol(),
                name = "${stakedToken.name} spooky farm",
                stakedToken = stakedToken.toFungibleToken(),
                rewardTokens = listOf(
                    reward.toFungibleToken()
                ),
                contractAddress = masterchef.address,
                vaultType = "spooky-masterchef",
                marketSize = marketSizeService.getMarketSize(
                    stakedToken.toFungibleToken(),
                    masterchef.address,
                    getNetwork()
                ),
                apr = aprCalculator.calculateApr(),
                balanceFetcher = FarmingPositionFetcher(
                    masterchef.address,
                    { user -> masterchef.userInfoFunction(index, user) }
                )
            )
        }
    }

    override fun getProtocol(): Protocol {
        return Protocol.SPOOKY
    }

    override fun getNetwork(): Network {
        return Network.FANTOM
    }
}