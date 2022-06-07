package io.defitrack.protocol.dodo.pooling

import io.defitrack.common.network.Network
import io.defitrack.pool.PoolingMarketService
import io.defitrack.pool.domain.PoolingMarketElement
import io.defitrack.protocol.DodoEthereumGraphProvider
import io.defitrack.protocol.Protocol
import io.defitrack.token.ERC20Resource
import io.defitrack.token.TokenType
import org.springframework.stereotype.Service

@Service
class DodoEthereumPoolingMarketProvider(
    erC20Resource: ERC20Resource,
    dodoEthereumGraphProvider: DodoEthereumGraphProvider,
) : DodoPoolingMarketProvider(erC20Resource, dodoEthereumGraphProvider) {
    override fun getProtocol(): Protocol {
        return Protocol.DODO
    }

    override fun getNetwork(): Network {
        return Network.ETHEREUM
    }
}