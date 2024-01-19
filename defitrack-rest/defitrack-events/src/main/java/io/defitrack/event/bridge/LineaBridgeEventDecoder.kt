package io.defitrack.event.bridge

import io.defitrack.common.network.Network
import io.defitrack.event.DefiEvent
import io.defitrack.event.DefiEventType
import io.defitrack.event.EventDecoder
import org.springframework.stereotype.Component
import org.web3j.protocol.core.methods.response.Log

private const val LINEA_L1_MESSAGE_SERVICE = "0xd19d4b5d358258f05d7b411e21a1460d11b0876f"

@Component
class LineaBridgeEventDecoder : EventDecoder() {
    override suspend fun appliesTo(log: Log, network: Network): Boolean {
        return network == Network.ETHEREUM &&
                log.address == LINEA_L1_MESSAGE_SERVICE
    }

    override suspend fun toDefiEvent(log: Log, network: Network): DefiEvent {
        return create(
            log = log,
            network = network,
            metadata = emptyMap(),
            type = DefiEventType.BRIDGE
        )
    }

    override fun eventTypes(): List<DefiEventType> {
        return listOf(DefiEventType.BRIDGE)
    }
}