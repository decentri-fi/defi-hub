package io.defitrack.protocol.application.stargate.pooling

import io.defitrack.common.network.Network
import io.defitrack.architecture.conditional.ConditionalOnCompany
import io.defitrack.protocol.Company
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component

@Component
@ConditionalOnCompany(Company.STARGATE)
@ConditionalOnProperty(value = ["ethereum.enabled"], havingValue = "true", matchIfMissing = true)
class SGethEthereumTokenProvider : AbstractSGethTokenProvider(
    address = "0x72e2f4830b9e45d52f80ac08cb2bec0fef72ed9c",
) {
    override fun getNetwork(): Network {
        return Network.ETHEREUM
    }
}