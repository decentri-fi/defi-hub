package io.defitrack.erc20

import io.defitrack.common.network.Network


class TokenInfo(
    val name: String,
    val network: Network,
    val address: String,
    val logo: String?
)