package io.defitrack

import java.math.BigInteger

class BonusDistributionStatus(
    val beneficiary: Boolean,
    val address: String,
    val claimed: Boolean,
    val index: Int,
    val proof: Array<String>,
    val amount: BigInteger
)