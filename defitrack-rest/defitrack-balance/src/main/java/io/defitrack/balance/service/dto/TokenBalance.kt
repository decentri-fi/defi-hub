package io.defitrack.balance.service.dto

import io.defitrack.common.network.Network
import io.defitrack.token.FungibleToken
import java.math.BigInteger

class TokenBalance(
    val amount: BigInteger,
    val token: FungibleToken,
    val network: Network
)