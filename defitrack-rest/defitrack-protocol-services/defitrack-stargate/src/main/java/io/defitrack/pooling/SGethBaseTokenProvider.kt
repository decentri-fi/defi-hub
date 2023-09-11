package io.defitrack.pooling

import io.defitrack.common.network.Network
import org.springframework.stereotype.Component

@Component
class SGethBaseTokenProvider : SGethTokenProvider(
    address = "0x224d8fd7ab6ad4c6eb4611ce56ef35dec2277f03"
) {
    override fun getNetwork(): Network {
        return Network.BASE
    }
}