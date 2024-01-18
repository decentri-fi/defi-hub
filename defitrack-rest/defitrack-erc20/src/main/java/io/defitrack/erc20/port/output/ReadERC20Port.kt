package io.defitrack.erc20.port.output

import arrow.core.Option
import io.defitrack.common.network.Network
import io.defitrack.erc20.ERC20

interface ReadERC20Port {
    suspend fun getERC20(network: Network, address: String): Option<ERC20>
}