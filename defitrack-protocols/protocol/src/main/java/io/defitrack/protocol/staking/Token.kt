package io.defitrack.protocol.staking

import io.defitrack.protocol.Protocol
import java.math.BigInteger

class Token(
    val name: String,
    val symbol: String,
    val address: String,
    val decimals: Int,
    val type: TokenType,
    val totalSupply: BigInteger = BigInteger.ZERO,
    val token0: Token?,
    val token1: Token?,
    val protocol: Protocol? = null
)