package io.defitrack.token

import java.math.BigInteger

open class FungibleToken(
    open val address: String,
    open val name: String,
    open val decimals: Int,
    open val symbol: String,
    open val logo: String?,
    open val type: String,
    open val totalSupply: BigInteger
)