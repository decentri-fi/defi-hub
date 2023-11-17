package io.defitrack.protocol.stargate.pooling

import io.defitrack.common.network.Network
import io.defitrack.conditional.ConditionalOnCompany
import io.defitrack.protocol.Company
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component

@Component
@ConditionalOnCompany(Company.STARGATE)
@ConditionalOnProperty(value = ["base.enabled"], havingValue = "true", matchIfMissing = true)
class SGethBaseTokenProvider : AbstractSGethTokenProvider(
    address = "0x224d8fd7ab6ad4c6eb4611ce56ef35dec2277f03"
) {
    override fun getNetwork(): Network {
        return Network.BASE
    }
}