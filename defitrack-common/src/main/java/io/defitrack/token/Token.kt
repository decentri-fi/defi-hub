package io.defitrack.token

import io.defitrack.protocol.Protocol
import java.math.BigInteger

class Token(
    val name: String,
    val symbol: String,
    val address: String,
    val decimals: Int,
    val type: TokenType,
    val totalSupply: BigInteger = BigInteger.ZERO,
    val token0: Token? = null,
    val token1: Token? = null,
    val protocol: Protocol? = null
)