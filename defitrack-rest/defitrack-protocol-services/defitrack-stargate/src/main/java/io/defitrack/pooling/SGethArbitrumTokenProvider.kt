package io.defitrack.pooling

import io.defitrack.common.network.Network
import org.springframework.stereotype.Component

@Component
class SGethArbitrumTokenProvider : SGethTokenProvider(
    address = "0x82cbecf39bee528b5476fe6d1550af59a9db6fc0"
) {
    override fun getNetwork(): Network {
        return Network.ARBITRUM
    }
}