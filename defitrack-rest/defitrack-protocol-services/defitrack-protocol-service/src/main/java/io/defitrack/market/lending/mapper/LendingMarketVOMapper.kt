package io.defitrack.market.lending.mapper

import io.defitrack.market.lending.domain.LendingMarket
import io.defitrack.market.lending.vo.LendingMarketVO
import io.defitrack.network.toVO
import io.defitrack.protocol.mapper.ProtocolVOMapper
import org.springframework.stereotype.Component

@Component
class LendingMarketVOMapper(
    private val protocolVOMapper: ProtocolVOMapper
) {

    fun map(market: LendingMarket): LendingMarketVO {
        return with(market) {
            LendingMarketVO(
                id = id,
                name = name,
                protocol = protocolVOMapper.map(protocol),
                network = network.toVO(),
                token = token,
                marketToken = marketToken,
                rate = rate?.toDouble(),
                poolType = poolType,
                marketSize = marketSize,
                prepareInvestmentSupported = investmentPreparer != null,
                exitPositionSupported = this.exitPositionPreparer != null,
                erc20Compatible = this.erc20Compatible,
                price = price,
                totalSupply = totalSupply
            )
        }
    }
}