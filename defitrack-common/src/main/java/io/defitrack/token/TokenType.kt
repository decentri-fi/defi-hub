package io.defitrack.token

enum class TokenType(val standardLpToken: Boolean = true) {
    UNISWAP, BALANCER(false), SUSHISWAP, SINGLE(false), DFYN, KYBER, HOP(false), CURVE(false), NATIVE, QUICKSWAP,
    IDEX, APE, DODO(false), BANCOR(false), SET(false), POOLTOGETHER(false), VELODROME, KYBER_ELASTIC,
    SOLIDLIZARD, STARGATE(false), STARGATE_VAULT(false), CAMELOT, BLUR(false), ALGEBRA_NFT(false)
}