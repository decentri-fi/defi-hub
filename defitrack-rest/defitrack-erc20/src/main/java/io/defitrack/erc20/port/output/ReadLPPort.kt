package io.defitrack.erc20.port.output

import io.defitrack.common.network.Network
import io.defitrack.market.pooling.contract.LPTokenContract

interface ReadLPPort {
    suspend fun getLP(network: Network, address: String): LPTokenContract
}