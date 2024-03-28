package io.defitrack.market.adapter.`in`.mapper

import io.defitrack.adapter.output.domain.market.GetPriceCommand
import io.defitrack.common.utils.FormatUtilsExtensions.asEth
import io.defitrack.erc20.toVO
import io.defitrack.market.adapter.`in`.resource.FarmingPositionVO
import io.defitrack.market.domain.farming.FarmingPosition
import io.defitrack.network.toVO
import io.defitrack.port.output.PriceClient
import io.defitrack.protocol.mapper.ProtocolVOMapper
import org.springframework.stereotype.Component

@Component
class FarmingPositionVOMapper(
    private val priceResource: PriceClient,
    private val protocolVOMapper: ProtocolVOMapper,
    private val farmingMarketVOMapper: FarmingMarketVOMapper
) {

    suspend fun map(farmingPosition: FarmingPosition): FarmingPositionVO {
        return with(farmingPosition) {

            val stakedInDollars = priceResource.calculatePrice(
                GetPriceCommand(
                    address = market.stakedToken.address,
                    network = market.network,
                    amount = underlyingAmount.asEth(market.stakedToken.decimals)
                )
            )

            FarmingPositionVO(
                id = market.id,
                network = market.network.toVO(),
                protocol = protocolVOMapper.map(market.protocol),
                dollarValue = stakedInDollars,
                name = market.name,
                apr = market.apr?.toDouble(),
                stakedToken = market.stakedToken.toVO(),
                rewardTokens = market.rewardTokens.map { it.toVO() },
                stakedAmountDecimal = underlyingAmount.asEth(market.stakedToken.decimals),
                stakedAmount = underlyingAmount.toString(10),
                exitPositionSupported = market.exitPositionPreparer != null,
                tokenAmount = tokenAmount.toString(10),
                tokenAmountDecimal = tokenAmount.asEth(market.stakedToken.decimals),
                expired = market.deprecated,
                market = farmingMarketVOMapper.map(market)
            )
        }
    }
}