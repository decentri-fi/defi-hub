package io.defitrack.claimable

import io.defitrack.common.network.Network
import io.defitrack.protocol.Protocol
import io.defitrack.token.FungibleToken
import io.defitrack.transaction.PreparedTransaction
import java.math.BigDecimal
import java.math.BigInteger


class Claimable(
    val id: String,
    val name: String,
    val type: String,
    val protocol: Protocol,
    val network: Network,
    val claimableTokens: List<FungibleToken>,
    val amount: BigInteger,
    val claimTransaction: PreparedTransaction
)

class Reward(
    val token: FungibleToken,
    val amount: BigDecimal
)
