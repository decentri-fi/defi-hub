package io.defitrack.protocol.sushiswap.pooling.arbitrum

import io.defitrack.common.network.Network
import io.defitrack.architecture.conditional.ConditionalOnCompany
import io.defitrack.protocol.Company
import io.defitrack.protocol.sushiswap.SushiswapService
import io.defitrack.protocol.sushiswap.pooling.DefaultSushiPoolingMarketProvider
import org.springframework.stereotype.Component

@Component
@ConditionalOnCompany(Company.SUSHISWAP)
class SushiswapArbitrumPoolingMarketProvider :
    DefaultSushiPoolingMarketProvider("0xc35dadb65012ec5796536bd9864ed8773abc74c4") {


    override fun getNetwork(): Network {
        return Network.ARBITRUM
    }
}