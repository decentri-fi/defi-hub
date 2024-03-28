package io.defitrack.protocol.application.dodo.pooling

import io.defitrack.common.network.Network
import io.defitrack.architecture.conditional.ConditionalOnCompany
import io.defitrack.protocol.Company
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.dodo.DodoEthereumGraphProvider
import org.springframework.stereotype.Service

@Service
@ConditionalOnCompany(Company.DODO)
class DodoEthereumPoolingMarketProvider(
    dodoEthereumGraphProvider: DodoEthereumGraphProvider,
) : DodoPoolingMarketProvider(dodoEthereumGraphProvider) {
    override fun getProtocol(): Protocol {
        return Protocol.DODO
    }

    override fun getNetwork(): Network {
        return Network.ETHEREUM
    }
}