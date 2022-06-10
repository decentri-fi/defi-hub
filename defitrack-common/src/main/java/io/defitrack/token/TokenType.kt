package io.defitrack.token

enum class TokenType(val standardLpToken: Boolean = true) {
    UNISWAP, BALANCER(false), SUSHISWAP, SINGLE(false), WAULT, DFYN, KYBER, SPOOKY, SPIRIT, HOP(false), CURVE(false), NATIVE, QUICKSWAP,
    IDEX, APE, DODO(false)
}