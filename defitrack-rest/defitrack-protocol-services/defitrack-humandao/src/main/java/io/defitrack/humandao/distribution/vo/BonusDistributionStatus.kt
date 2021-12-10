package io.defitrack.humandao.distribution.vo

import java.math.BigInteger

class BonusDistributionStatus(
    val beneficiary: Boolean,
    val address: String,
    val claimed: Boolean,
    val index: Int,
    val proof: Array<String>,
    val maxBonusAmount: BigInteger,
    val currentBonusAmount: BigInteger,
    val shouldFillUpBalance: Boolean
)