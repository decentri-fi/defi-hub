package io.defitrack.market.pooling.vo

import io.defitrack.common.utils.FormatUtilsExtensions.asEth
import io.defitrack.erc20.FungibleTokenInformationVO
import io.defitrack.erc20.toVO
import io.defitrack.market.domain.PoolingMarketTokenShare
import java.math.BigDecimal
import java.math.BigInteger

class PoolingMarketTokenShareVO(
    val token: FungibleTokenInformationVO,
    val reserve: BigInteger,
    val reserveDecimal: BigDecimal
)

fun PoolingMarketTokenShare.toVO(): PoolingMarketTokenShareVO {
    return PoolingMarketTokenShareVO(
        token = this.token.toVO(),
        reserve = this.reserve,
        reserveDecimal = this.reserve.asEth(this.token.decimals)
    )
}