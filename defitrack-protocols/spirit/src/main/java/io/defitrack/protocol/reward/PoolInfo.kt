package io.defitrack.protocol.reward

import java.math.BigInteger

class PoolInfo(
    val lpToken: String,
    val allocPoint: BigInteger,
    val lastRewardBlockingQueue: BigInteger,
    val accSpiritPerShare: BigInteger,
    val depositFeeBP: BigInteger,
)