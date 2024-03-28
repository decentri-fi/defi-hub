package io.defitrack.market.pooling.mapper

import io.defitrack.adapter.output.domain.market.GetPriceCommand
import io.defitrack.common.utils.BigDecimalExtensions.dividePrecisely
import io.defitrack.common.utils.BigDecimalExtensions.isZero
import io.defitrack.common.utils.FormatUtilsExtensions.asEth
import io.defitrack.erc20.toVO
import io.defitrack.market.domain.PoolingMarket
import io.defitrack.market.domain.PoolingMarketTokenShare
import io.defitrack.market.pooling.vo.PoolingMarketTokenShareVO
import io.defitrack.market.pooling.vo.PoolingPositionTokenshareVO
import io.defitrack.port.output.PriceClient
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.math.BigInteger

@Service
class PoolingBreakdownVOMapper(private val prices: PriceClient) {

    val logger = LoggerFactory.getLogger(this::class.java)

    suspend fun toVO(tokenShares: List<PoolingMarketTokenShare>?): List<PoolingMarketTokenShareVO> {
        if (tokenShares.isNullOrEmpty()) {
            return emptyList()
        }

        return tokenShares.map { share ->
            PoolingMarketTokenShareVO(
                token = share.token.toVO(),
                reserve = share.reserve,
                reserveDecimal = share.reserve.asEth(share.token.decimals)
            )
        }
    }

    suspend fun toPositionVO(poolingMarket: PoolingMarket, userSupply: BigDecimal): List<PoolingPositionTokenshareVO> {
        val toVO = toVO(poolingMarket.breakdown?.get())
        if (poolingMarket.totalSupply == BigInteger.ZERO) {
            logger.warn("totalSupply is zero for ${poolingMarket.name}")
            return emptyList()
        }

        if (userSupply.isZero()) {
            return emptyList()
        }

        val ratio = userSupply.dividePrecisely(poolingMarket.totalSupply.get())

        return toVO.map {

            val reserveUsd = prices.calculatePrice(
                GetPriceCommand(
                    address = it.token.address,
                    network = it.token.network.toNetwork(),
                    amount = it.reserve.asEth(it.token.decimals)
                )
            ).toBigDecimal()

            PoolingPositionTokenshareVO(
                token = it.token,
                reserve = it.reserve.toBigDecimal().times(ratio).toBigInteger(),
                reserveDecimal = it.reserveDecimal.times(ratio),
                reserveUSD = reserveUsd.times(ratio)
            )
        }
    }
}