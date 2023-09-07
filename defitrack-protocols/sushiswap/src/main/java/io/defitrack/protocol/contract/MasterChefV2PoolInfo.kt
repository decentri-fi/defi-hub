package io.defitrack.protocol.contract

import java.math.BigInteger

class MasterChefV2PoolInfo(
    val accTokenPerShare: BigInteger,
    val lastRewardBlock: BigInteger,
    val allocPoint: BigInteger,
)