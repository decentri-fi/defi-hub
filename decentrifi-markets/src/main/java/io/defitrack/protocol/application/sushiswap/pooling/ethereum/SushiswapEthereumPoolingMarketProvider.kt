package io.defitrack.protocol.sushiswap.pooling.ethereum

import io.defitrack.common.network.Network
import io.defitrack.architecture.conditional.ConditionalOnCompany
import io.defitrack.protocol.Company
import io.defitrack.protocol.sushiswap.pooling.DefaultSushiPoolingMarketProvider
import org.springframework.stereotype.Component

@Component
@ConditionalOnCompany(Company.SUSHISWAP)
class SushiswapEthereumPoolingMarketProvider :
    DefaultSushiPoolingMarketProvider("0xC0AEe478e3658e2610c5F7A4A2E1777cE9e4f2Ac") {
    override fun getNetwork(): Network {
        return Network.ETHEREUM
    }
}