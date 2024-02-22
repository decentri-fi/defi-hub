package io.defitrack.protocol.dodo.pooling

import io.defitrack.common.network.Network
import io.defitrack.architecture.conditional.ConditionalOnCompany
import io.defitrack.protocol.Company
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.dodo.DodoArbitrumGraphProvider
import org.springframework.stereotype.Service

@Service
@ConditionalOnCompany(Company.DODO)
//TODO: comes from graph. Need remove this
class DodoArbitrumPoolingMarketProvider(
    dodoGraphProvider: DodoArbitrumGraphProvider,
) : DodoPoolingMarketProvider(dodoGraphProvider) {

    override fun getProtocol(): Protocol {
        return Protocol.DODO
    }

    override fun getNetwork(): Network {
        return Network.ARBITRUM
    }
}