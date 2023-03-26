package io.defitrack.farming

import io.defitrack.apr.MinichefStakingAprCalculator
import io.defitrack.common.network.Network
import io.defitrack.market.farming.FarmingMarketProvider
import io.defitrack.market.farming.domain.FarmingMarket
import io.defitrack.market.lending.domain.PositionFetcher
import io.defitrack.protocol.ContractType
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.SpiritFantomService
import io.defitrack.protocol.reward.MasterchefLpContract
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.springframework.stereotype.Component
import java.math.BigDecimal

@Component
class SpiritFantomFarmingMarketProvider(
    private val spiritFantomService: SpiritFantomService,
) : FarmingMarketProvider() {

    override suspend fun fetchMarkets(): List<FarmingMarket> = coroutineScope {
        val masterchef = MasterchefLpContract(
            getBlockchainGateway(),
            getAbi("spirit/Masterchef.json"),
            spiritFantomService.getMasterchef()
        )

        val reward = getToken(masterchef.rewardToken())

        return@coroutineScope masterchef.poolInfos().mapIndexed { index, value ->
            async {
                try {
                    val stakedToken = getToken(value.lpToken)
                    val aprCalculator = MinichefStakingAprCalculator(
                        getERC20Resource(),
                        getPriceResource(),
                        masterchef,
                        index,
                        stakedToken
                    )
                    create(
                        identifier = "${masterchef.address}-${index}",
                        name = "${stakedToken.name} spirit farm",
                        stakedToken = stakedToken.toFungibleToken(),
                        rewardTokens = listOf(
                            reward.toFungibleToken()
                        ),
                        vaultType = "spirit-masterchef",
                        marketSize = BigDecimal.ZERO,
                        apr = aprCalculator.calculateApr(),
                        balanceFetcher = PositionFetcher(
                            masterchef.address,
                            { user -> masterchef.userInfoFunction(index, user) }
                        ),
                        farmType = ContractType.LIQUIDITY_MINING
                    )
                } catch (ex: Exception) {
                    logger.error("Error while fetching spirit market", ex)
                    null
                }
            }
        }.awaitAll().filterNotNull()
    }

    override fun getProtocol(): Protocol {
        return Protocol.SPIRITSWAP
    }

    override fun getNetwork(): Network {
        return Network.FANTOM
    }
}