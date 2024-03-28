package io.defitrack.adapter.output.domain.market

import io.defitrack.adapter.output.domain.erc20.FungibleTokenInformation
import java.math.BigDecimal
import java.math.BigInteger

class PoolingMarketTokenShareInformationDTO(
    val token: FungibleTokenInformation,
    val reserve: BigInteger,
    val reserveDecimal: BigDecimal
)