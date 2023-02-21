package io.defitrack.farming

import io.defitrack.common.network.Network
import io.defitrack.market.farming.FarmingMarketProvider
import io.defitrack.market.farming.domain.FarmingMarket
import io.defitrack.price.PriceResource
import io.defitrack.protocol.ContractType
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.SpookyFantomService
import io.defitrack.protocol.contract.MasterChefBasedContract
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.springframework.stereotype.Component

@Component
class SpookyFarmingMarketProvider(
    private val spookyFantomService: SpookyFantomService,
    private val priceResource: PriceResource,
) : FarmingMarketProvider() {

    override suspend fun fetchMarkets(): List<FarmingMarket> = coroutineScope {
        val masterchef = MasterChefBasedContract(
            "boo",
            "booPerSecond",
            "pendingBOO",
            getBlockchainGateway(),
            spookyFantomService.getMasterchef()
        )

        val reward = getToken(masterchef.rewardToken())

        return@coroutineScope masterchef.poolInfos().mapIndexed { index, value ->

            val stakedToken = getToken(value.lpToken)
            val aprCalculator = MinichefStakingAprCalculator(
                getERC20Resource(),
                priceResource,
                masterchef,
                index,
                stakedToken
            )
            async {
                try {
                    create(
                        identifier = "${masterchef.address}-${index}",
                        name = "${stakedToken.name} spooky farm",
                        stakedToken = stakedToken.toFungibleToken(),
                        rewardTokens = listOf(
                            reward.toFungibleToken()
                        ),
                        vaultType = "spooky-masterchef",
                        marketSize = marketSizeService.getMarketSize(
                            stakedToken.toFungibleToken(),
                            masterchef.address,
                            getNetwork()
                        ),
                        apr = aprCalculator.calculateApr(),
                        balanceFetcher = defaultPositionFetcher(masterchef.address),
                        farmType = ContractType.LIQUIDITY_MINING
                    )
                } catch (ex: Exception) {
                    logger.error("Error while fetching spooky farm", ex)
                    null
                }
            }
        }.awaitAll().filterNotNull()
    }

    override fun getProtocol(): Protocol {
        return Protocol.SPOOKY
    }

    override fun getNetwork(): Network {
        return Network.FANTOM
    }
}