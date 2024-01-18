package io.defitrack.claimable.domain

import io.defitrack.common.network.Network
import io.defitrack.token.FungibleToken
import io.defitrack.protocol.Protocol
import io.defitrack.transaction.PreparedTransaction
import java.math.BigInteger


class UserClaimable(
    val id: String,
    val name: String,
    val protocol: Protocol,
    val network: Network,
    val claimableToken: FungibleToken,
    val amount: BigInteger,
    val claimTransaction: PreparedTransaction? = null
)