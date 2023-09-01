package io.defitrack.events.bridge

import io.defitrack.common.network.Network
import io.defitrack.event.DefiEvent
import io.defitrack.event.DefiEventType
import io.defitrack.event.EventDecoder
import io.defitrack.network.toVO
import org.springframework.stereotype.Component
import org.web3j.protocol.core.methods.response.Log

private const val LINEA_L1_MESSAGE_SERVICE = "0xd19d4b5d358258f05d7b411e21a1460d11b0876f"

@Component
class LineaBridgeEventDecoder : EventDecoder() {
    override fun appliesTo(log: Log, network: Network): Boolean {
        return network == Network.ETHEREUM &&
                log.address == LINEA_L1_MESSAGE_SERVICE
    }

    override suspend fun extract(log: Log, network: Network): DefiEvent {
        return DefiEvent(
            transactionId = log.transactionHash,
            network = network.toVO(),
            metadata = emptyMap(),
            type = DefiEventType.BRIDGE
        )
    }

    override fun eventTypes(): List<DefiEventType> {
        return listOf(DefiEventType.BRIDGE)
    }
}