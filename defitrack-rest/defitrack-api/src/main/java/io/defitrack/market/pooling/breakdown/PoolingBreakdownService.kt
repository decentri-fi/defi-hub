package io.defitrack.market.pooling.breakdown

import io.defitrack.common.utils.BigDecimalExtensions.dividePrecisely
import io.defitrack.market.pooling.domain.PoolingMarket
import io.defitrack.market.pooling.domain.PoolingMarketTokenShare
import io.defitrack.market.pooling.vo.PoolingMarketTokenShareVO
import io.defitrack.market.pooling.vo.PoolingPositionTokenshareVO
import org.springframework.stereotype.Service
import java.math.BigInteger

@Service
class PoolingBreakdownService {

    fun toVO(tokenShares: List<PoolingMarketTokenShare>?): List<PoolingMarketTokenShareVO> {
        if (tokenShares.isNullOrEmpty()) {
            return emptyList()
        }

        return tokenShares.map {
            PoolingMarketTokenShareVO(
                token = it.token,
                reserve = it.reserve,
                reserveUSD = it.reserveUSD,
            )
        }
    }

    fun toPositionVO(poolingMarket: PoolingMarket, userSupply: BigInteger): List<PoolingPositionTokenshareVO> {
        val toVO = toVO(poolingMarket.breakdown)
        val ratio = userSupply.toBigDecimal().dividePrecisely(poolingMarket.totalSupply.toBigDecimal())

        return toVO.map {
            PoolingPositionTokenshareVO(
                token = it.token,
                reserve = it.reserve.toBigDecimal().times(ratio).toBigInteger(),
                reserveUSD = it.reserveUSD.times(ratio)
            )
        }
    }
}