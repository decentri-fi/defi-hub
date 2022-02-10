package io.defitrack.protocol.staking

enum class TokenType(val standardLpToken: Boolean = true) {
    UNISWAP, BALANCER(false), SUSHISWAP, DMM, SINGLE(false), WAULT, DFYN, KYBER, SPOOKY, SPIRIT, HOP
}