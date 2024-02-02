package io.defitrack.market.pooling.mapper

import io.defitrack.common.utils.BigDecimalExtensions.dividePrecisely
import io.defitrack.common.utils.BigDecimalExtensions.isZero
import io.defitrack.common.utils.FormatUtilsExtensions.asEth
import io.defitrack.market.domain.PoolingMarket
import io.defitrack.market.domain.PoolingMarketTokenShare
import io.defitrack.market.pooling.vo.PoolingMarketTokenShareVO
import io.defitrack.market.pooling.vo.PoolingPositionTokenshareVO
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.math.BigInteger

@Service
class PoolingBreakdownVOMapper {

    val logger = LoggerFactory.getLogger(this::class.java)

    fun toVO(tokenShares: List<PoolingMarketTokenShare>?): List<PoolingMarketTokenShareVO> {
        if (tokenShares.isNullOrEmpty()) {
            return emptyList()
        }

        return tokenShares.map {
            PoolingMarketTokenShareVO(
                token = it.token,
                reserve = it.reserve,
                reserveUSD = it.reserveUSD,
                reserveDecimal = it.reserve.asEth(it.token.decimals)
            )
        }
    }

    fun toPositionVO(poolingMarket: PoolingMarket, userSupply: BigDecimal): List<PoolingPositionTokenshareVO> {
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
            PoolingPositionTokenshareVO(
                token = it.token,
                reserve = it.reserve.toBigDecimal().times(ratio).toBigInteger(),
                reserveDecimal = it.reserveDecimal.times(ratio),
                reserveUSD = (it.reserveUSD ?: BigDecimal.ZERO).times(ratio)
            )
        }
    }
}