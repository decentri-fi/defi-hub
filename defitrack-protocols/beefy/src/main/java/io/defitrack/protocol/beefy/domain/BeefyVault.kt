package io.defitrack.protocol.beefy.domain

import java.math.BigInteger

class BeefyVault(
    val id: String,
    val name: String,
    val token: String,
    val tokenAddress: String?,
    val tokenDecimals: Int,
    val earnedToken: String,
    val earnedTokenAddress: String,
    val earnContractAddress: String,
    val pricePerFullShare: BigInteger,
    val status: String,
    val platform: String,
    val chain: String,
)