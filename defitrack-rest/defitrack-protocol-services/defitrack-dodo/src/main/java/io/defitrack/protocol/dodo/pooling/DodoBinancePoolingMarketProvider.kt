package io.defitrack.protocol.dodo.pooling

import io.defitrack.common.network.Network
import io.defitrack.pool.domain.PoolingMarketElement
import io.defitrack.protocol.DodoBinanceGraphProvider
import io.defitrack.protocol.Protocol
import io.defitrack.token.ERC20Resource
import io.defitrack.token.TokenType
import org.springframework.stereotype.Service

@Service
class DodoBinancePoolingMarketProvider(
    erC20Resource: ERC20Resource,
    dodoProvider: DodoBinanceGraphProvider,
) : DodoPoolingMarketProvider(erC20Resource, dodoProvider) {

    override fun getProtocol(): Protocol {
        return Protocol.DODO
    }

    override fun getNetwork(): Network {
        return Network.BINANCE
    }
}