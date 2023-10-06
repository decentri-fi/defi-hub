package io.defitrack.protocol.stargate.pooling

import io.defitrack.common.network.Network
import io.defitrack.conditional.ConditionalOnCompany
import io.defitrack.protocol.Company
import org.springframework.stereotype.Component

@Component
@ConditionalOnCompany(Company.STARGATE)
class SGethArbitrumTokenProvider : AbstractSGethTokenProvider(
    address = "0x82cbecf39bee528b5476fe6d1550af59a9db6fc0"
) {
    override fun getNetwork(): Network {
        return Network.ARBITRUM
    }
}