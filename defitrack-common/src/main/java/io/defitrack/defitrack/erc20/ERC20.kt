package io.defitrack.erc20

import io.defitrack.common.network.Network

class ERC20(
    val name: String,
    val symbol: String,
    val decimals: Int,
    val network: Network,
    val address: String,
)