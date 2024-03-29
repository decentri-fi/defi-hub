package io.defitrack.claim

import io.defitrack.adapter.output.domain.erc20.FungibleTokenInformation
import io.defitrack.common.network.Network
import io.defitrack.protocol.Protocol
import io.defitrack.transaction.PreparedTransaction
import java.math.BigInteger


class UserClaimable(
    val id: String,
    val name: String,
    val protocol: Protocol,
    val network: Network,
    val claimableToken: FungibleTokenInformation,
    val amount: BigInteger,
    val claimTransaction: PreparedTransaction? = null
)