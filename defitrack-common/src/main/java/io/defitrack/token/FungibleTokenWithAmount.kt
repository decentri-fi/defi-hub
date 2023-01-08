package io.defitrack.token

import java.math.BigDecimal

class FungibleTokenWithAmount(
    val amount: BigDecimal?,
    address: String,
    name: String,
    decimals: Int,
    symbol: String,
    logo: String?,
    type: TokenType
) : FungibleToken(address, name, decimals, symbol, logo, type)
