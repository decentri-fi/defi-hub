package io.defitrack.protocol.beefy.domain

class BeefyVault(
    val id: String,
    val name: String,
    val token: String,
    val tokenAddress: String?,
    val tokenDecimals: Int,
    val earnedToken: String,
    val earnedTokenAddress: String,
    val earnContractAddress: String,
    val status: String,
    val chain: String,
)