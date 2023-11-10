package io.defitrack.protocol.beefy.domain

data class BeefyLaunchPool(
    val id: String,
    val earnedTokenAddress: String,
    val earnContractAddress: String,
    val chain: String,
    val status: String,
    val periodFinish: Long
)