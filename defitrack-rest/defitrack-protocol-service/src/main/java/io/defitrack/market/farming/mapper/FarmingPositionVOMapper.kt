package io.defitrack.market.farming.mapper

import io.defitrack.common.utils.FormatUtilsExtensions.asEth
import io.defitrack.domain.GetPriceCommand
import io.defitrack.domain.toNetworkInformation
import io.defitrack.market.farming.domain.FarmingPosition
import io.defitrack.market.farming.vo.FarmingPositionVO
import io.defitrack.port.input.PriceResource
import io.defitrack.protocol.mapper.ProtocolVOMapper
import org.springframework.stereotype.Component

@Component
class FarmingPositionVOMapper(
    private val priceResource: PriceResource,
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
                network = market.network.toNetworkInformation(),
                protocol = protocolVOMapper.map(market.protocol),
                dollarValue = stakedInDollars,
                name = market.name,
                apr = market.apr?.toDouble(),
                stakedToken = market.stakedToken,
                rewardTokens = market.rewardTokens,
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