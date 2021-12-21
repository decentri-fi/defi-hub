package io.defitrack.humandao.distribution.vo

class BonusDistributionStatus(
    val beneficiary: Boolean,
    val address: String,
    val claimed: Boolean,
    val index: Int,
    val proof: Array<String>,
    val maxBonusAmount: String,
    val currentBonusAmount: String,
    val shouldFillUpBalance: Boolean
)