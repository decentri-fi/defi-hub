package io.defitrack.farming

import io.defitrack.MarketFactory
import io.defitrack.common.network.Network
import io.defitrack.market.farming.FarmingMarketProvider
import io.defitrack.market.farming.domain.FarmingMarket
import io.defitrack.protocol.FarmType
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.SpookyFantomService
import io.defitrack.protocol.reward.MasterchefLpContract
import io.defitrack.token.ERC20Resource
import org.springframework.stereotype.Component

@Component
class SpookyFarmingMarketProvider(
    private val spookyFantomService: SpookyFantomService,
    private val marketFactory: MarketFactory,
) : FarmingMarketProvider() {

    override suspend fun fetchMarkets(): List<FarmingMarket> {
        val masterchef = MasterchefLpContract(
            marketFactory.blockchainGatewayProvider.getGateway(getNetwork()),
            marketFactory.abiResource.getABI("spooky/Masterchef.json"),
            spookyFantomService.getMasterchef()
        )

        val reward = marketFactory.erC20Resource.getTokenInformation(getNetwork(), masterchef.rewardToken())

        return masterchef.poolInfos().mapIndexed { index, value ->

            val stakedToken = marketFactory.erC20Resource.getTokenInformation(getNetwork(), value.lpToken)
            val aprCalculator = MinichefStakingAprCalculator(
                marketFactory.erC20Resource,
                marketFactory.priceResource,
                masterchef,
                index,
                stakedToken
            )
            create(
                identifier = "${masterchef.address}-${index}",
                name = "${stakedToken.name} spooky farm",
                stakedToken = stakedToken.toFungibleToken(),
                rewardTokens = listOf(
                    reward.toFungibleToken()
                ),
                vaultType = "spooky-masterchef",
                marketSize = marketFactory.marketSizeService.getMarketSize(
                    stakedToken.toFungibleToken(),
                    masterchef.address,
                    getNetwork()
                ),
                apr = aprCalculator.calculateApr(),
                balanceFetcher = defaultPositionFetcher(masterchef.address),
                farmType = FarmType.LIQUIDITY_MINING
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