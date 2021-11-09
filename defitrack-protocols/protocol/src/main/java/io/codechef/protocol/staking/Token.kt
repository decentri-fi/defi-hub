package io.defitrack.protocol.staking

import java.math.BigInteger

open class Token(
    val name: String,
    val symbol: String,
    val address: String,
    val decimals: Int,
    val type: TokenType
)

class LpToken(
    name: String,
    symbol: String,
    address: String,
    decimals: Int,
    type: TokenType,
    val totalSupply: BigInteger,
    val token0: Token,
    val token1: Token,
) : Token(name, symbol, address, decimals, type)

class SingleToken(name: String, symbol: String, address: String, decimals: Int) :
    Token(name, symbol, address, decimals, TokenType.SINGLE)