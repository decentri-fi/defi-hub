package io.defitrack.apr

import io.defitrack.common.network.Network
import io.defitrack.protocol.staking.TokenType
import java.math.BigDecimal
import java.math.BigInteger

class StakedAsset(
    val address: String,
    val network: Network,
    val amount: BigDecimal,
    val tokenType: TokenType
)