package io.defitrack.events

import io.defitrack.abi.TypeUtils.Companion.address
import io.defitrack.abi.TypeUtils.Companion.uint256
import io.defitrack.common.network.Network
import io.defitrack.event.DefiEvent
import io.defitrack.event.DefiEventType
import io.defitrack.event.EventDecoder
import io.defitrack.event.EventUtils.Companion.appliesTo
import org.springframework.stereotype.Component
import org.web3j.abi.FunctionReturnDecoder
import org.web3j.abi.datatypes.Event
import org.web3j.protocol.core.methods.response.Log
import java.math.BigInteger

@Component
class TransferEventDecoder : EventDecoder() {

    val transferEvent = Event("Transfer", listOf(address(true), address(true), uint256()))

    override fun appliesTo(log: Log): Boolean {
        return log.appliesTo(transferEvent)
    }

    override suspend fun extract(log: Log, network: Network): DefiEvent {
        val from =
            "from" to FunctionReturnDecoder.decodeIndexedValue(
                log.topics[1], address()
            ).value as String;

        val to = "to" to FunctionReturnDecoder.decodeIndexedValue(
            log.topics[2], address()
        ).value as String;

        val amount = "amount" to FunctionReturnDecoder.decode(
            log.data,
            transferEvent.nonIndexedParameters
        )[0].value as BigInteger

        val asset = "asset" to getToken(log.address, network)

        return DefiEvent(
            type = DefiEventType.TRANSFER,
            metadata = mapOf(from, to, asset, amount)
        )
    }
}