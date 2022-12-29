package io.defitrack.farming

import io.defitrack.abi.ABIResource
import io.defitrack.apr.MinichefStakingAprCalculator
import io.defitrack.common.network.Network
import io.defitrack.evm.contract.BlockchainGatewayProvider
import io.defitrack.market.farming.FarmingMarketProvider
import io.defitrack.price.PriceResource
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.SpiritFantomService
import io.defitrack.protocol.reward.MasterchefLpContract
import io.defitrack.market.farming.domain.FarmingMarket
import io.defitrack.market.lending.domain.PositionFetcher
import io.defitrack.protocol.FarmType
import io.defitrack.token.ERC20Resource
import org.springframework.stereotype.Component
import java.math.BigDecimal

@Component
class SpiritFantomFarmingMarketProvider(
    private val spiritFantomService: SpiritFantomService,
    private val abiResource: ABIResource,
    private val priceResource: PriceResource,
) : FarmingMarketProvider() {

    override suspend fun fetchMarkets(): List<FarmingMarket> {
        val masterchef = MasterchefLpContract(
            blockchainGatewayProvider.getGateway(getNetwork()),
            abiResource.getABI("spirit/Masterchef.json"),
            spiritFantomService.getMasterchef()
        )

        val reward = erC20Resource.getTokenInformation(getNetwork(), masterchef.rewardToken())

        return masterchef.poolInfos().mapIndexed { index, value ->

            val stakedToken = erC20Resource.getTokenInformation(getNetwork(), value.lpToken)
            val aprCalculator = MinichefStakingAprCalculator(
                erC20Resource,
                priceResource,
                masterchef,
                index,
                stakedToken
            )
            FarmingMarket(
                id = "fantom-spirit-${masterchef.address}-${index}",
                network = getNetwork(),
                protocol = getProtocol(),
                name = "${stakedToken.name} spirit farm",
                stakedToken = stakedToken.toFungibleToken(),
                rewardTokens = listOf(
                    reward.toFungibleToken()
                ),
                contractType = "spirit-masterchef",
                marketSize = BigDecimal.ZERO,
                apr = aprCalculator.calculateApr(),
                balanceFetcher = PositionFetcher(
                    masterchef.address,
                    { user -> masterchef.userInfoFunction(index, user) }
                ),
                farmType = FarmType.LIQUIDITY_MINING
            )
        }
    }

    override fun getProtocol(): Protocol {
        return Protocol.SPIRITSWAP
    }

    override fun getNetwork(): Network {
        return Network.FANTOM
    }
}