package io.defitrack.protocol.dodo.pooling

import io.defitrack.common.network.Network
import io.defitrack.price.PriceResource
import io.defitrack.protocol.DodoBinanceGraphProvider
import io.defitrack.protocol.Protocol
import org.springframework.stereotype.Service

@Service
class DodoBinancePoolingMarketProvider(
    dodoProvider: DodoBinanceGraphProvider,
) : DodoPoolingMarketProvider(dodoProvider) {

    override fun getProtocol(): Protocol {
        return Protocol.DODO
    }

    override fun getNetwork(): Network {
        return Network.BINANCE
    }
}