package io.defitrack.event

import io.defitrack.domain.NetworkInformation
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.protocol.Protocol

class DefiEvent(
    val id: String,
    val transaction: BlockchainGateway.TransactionVO,
    val network: NetworkInformation,
    val type: DefiEventType,
    val protocol: Protocol? = null,
    val metadata: Map<String, Any>
)
