package io.defitrack.protocol.beefy.domain

class BeefyVault(
    val id: String,
    val name: String,
    val token: String,
    val earnContractAddress: String,
    val chain: String,
    val status: String
)