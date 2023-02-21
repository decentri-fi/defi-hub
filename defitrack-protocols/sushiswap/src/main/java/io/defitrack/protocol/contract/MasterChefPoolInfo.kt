package io.defitrack.protocol.contract

import java.math.BigInteger

class MasterChefPoolInfo(
    val lpToken: String,
    val allocPoint: BigInteger,
    val lastRewardBlock: BigInteger,
    val accTokenPerShare: BigInteger
)