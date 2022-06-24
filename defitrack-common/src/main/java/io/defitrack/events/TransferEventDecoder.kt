package io.defitrack.events

import io.defitrack.abi.TypeUtils.Companion.address
import io.defitrack.abi.TypeUtils.Companion.uint256
import io.defitrack.events.EventUtils.Companion.appliesTo
import org.springframework.stereotype.Component
import org.web3j.abi.FunctionReturnDecoder
import org.web3j.abi.datatypes.Event
import org.web3j.protocol.core.methods.response.Log

@Component
class TransferEventDecoder() : EventDecoder {

    val transferEvent = Event("Transfer", listOf(address(), address(), uint256()))

    override fun appliesTo(log: Log): Boolean {
        return log.appliesTo(transferEvent)
    }

    override fun extract(log: Log): DefiEvent {
        return DefiEvent(
            type = DefiEventType.TRANSFER,
            metadata = mapOf(
                "from" to FunctionReturnDecoder.decodeIndexedValue(
                    log.topics[1], address()
                ).value as String
            )
        )
    }
}