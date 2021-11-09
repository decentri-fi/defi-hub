package io.codechef.defitrack.erc20

import io.codechef.common.network.Network

class ERC20(
    val name: String,
    val symbol: String,
    val decimals: Int,
    val network: Network,
    val address: String,
)