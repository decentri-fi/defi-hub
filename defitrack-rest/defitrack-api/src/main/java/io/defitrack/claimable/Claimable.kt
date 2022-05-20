package io.defitrack.claimable

import io.defitrack.common.network.Network
import io.defitrack.protocol.Protocol
import io.defitrack.token.FungibleToken
import io.defitrack.transaction.PreparedTransaction
import java.math.BigInteger


class Claimable(
    val id: String,
    val name: String,
    val address: String,
    val type: String,
    val protocol: Protocol,
    val network: Network,
    val claimableToken: FungibleToken,
    val amount: BigInteger,
    val claimTransaction: List<PreparedTransaction> = emptyList()
)