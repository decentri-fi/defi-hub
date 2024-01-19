package io.defitrack.balance.service.dto

import io.defitrack.common.network.Network
import io.defitrack.erc20.domain.FungibleTokenInformation
import java.math.BigInteger

class TokenBalance(
    val amount: BigInteger,
    val token: FungibleTokenInformation,
    val network: Network
)