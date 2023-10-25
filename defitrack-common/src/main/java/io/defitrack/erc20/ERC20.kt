package io.defitrack.erc20

import io.defitrack.common.network.Network
import io.defitrack.common.utils.Refreshable
import java.math.BigInteger

class ERC20(
    val name: String,
    val symbol: String,
    val decimals: Int,
    val network: Network,
    val address: String,
    val totalSupply: Refreshable<BigInteger>,
)