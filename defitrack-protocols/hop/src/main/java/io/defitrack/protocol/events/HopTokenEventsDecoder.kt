package io.defitrack.protocol.events

import io.defitrack.abi.TypeUtils.Companion.address
import io.defitrack.abi.TypeUtils.Companion.uint256
import io.defitrack.events.DefiEvent
import io.defitrack.events.DefiEventType
import io.defitrack.events.EventDecoder
import io.defitrack.protocol.Protocol
import org.springframework.stereotype.Component
import org.web3j.abi.EventEncoder
import org.web3j.abi.FunctionReturnDecoder
import org.web3j.protocol.core.methods.response.Log
import java.math.BigInteger


@Component
class HopClaimEventDecoder : EventDecoder {

    val claimEvent = org.web3j.abi.datatypes.Event("Claim", listOf(address(true), uint256()))

    override fun appliesTo(log: Log): Boolean {
        return log.address.lowercase() == "0xc5102fe9359fd9a28f877a67e36b0f050d81a3cc"
                && log.topics.map { it.lowercase() }.contains(EventEncoder.encode(claimEvent))
    }

    override fun extract(log: Log): DefiEvent {
        val user = FunctionReturnDecoder.decodeIndexedValue(
            log.topics[1], address()
        ).value as String

        val amount = FunctionReturnDecoder.decode(
            log.data,
            claimEvent.nonIndexedParameters
        )[0].value as BigInteger

        return DefiEvent(
            type = DefiEventType.CLAIM,
            protocol = Protocol.HOP,
            metadata = mapOf("user" to user, "amount" to amount)
        )
    }
}