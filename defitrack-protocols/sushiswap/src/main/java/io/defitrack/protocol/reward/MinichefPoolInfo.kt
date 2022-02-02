package io.defitrack.protocol.reward

import java.math.BigInteger

class MinichefPoolInfo(
    val accSushiPerShare: BigInteger,
    val lastRewardTime: BigInteger,
    val allocPoint: BigInteger
)