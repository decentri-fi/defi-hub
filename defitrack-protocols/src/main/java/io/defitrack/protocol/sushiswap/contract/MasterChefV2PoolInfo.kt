package io.defitrack.protocol.sushiswap.contract

import java.math.BigInteger

class MasterChefV2PoolInfo(
    val accTokenPerShare: BigInteger,
    val lastRewardBlock: BigInteger,
    val allocPoint: BigInteger,
)