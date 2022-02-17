package io.defitrack.token

enum class TokenType(val standardLpToken: Boolean = true) {
    UNISWAP, BALANCER(false), SUSHISWAP, DMM, SINGLE(false), WAULT, DFYN, KYBER, SPOOKY, SPIRIT, HOP, CURVE(false)
}