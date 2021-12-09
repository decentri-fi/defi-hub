package io.defitrack

class MerkleConfig(
    val merkleRoot: String,
    val tokenTotal: String,
    val claims: Map<String, Claim>
)

class Claim(
    val index: Int,
    val amount: String,
    val proof: Array<String>
)

