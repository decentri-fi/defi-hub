package io.defitrack.balance.service.dto

import io.defitrack.common.network.Network
import io.defitrack.erc20.FungibleTokenInformationVO
import java.math.BigInteger

class TokenBalanceVO(
    val amount: BigInteger,
    val token: FungibleTokenInformationVO,
    val network: Network
)