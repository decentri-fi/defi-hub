package io.defitrack.events.claim

import io.defitrack.abi.TypeUtils.Companion.address
import io.defitrack.abi.TypeUtils.Companion.uint256
import io.defitrack.common.network.Network
import io.defitrack.event.DefiEvent
import io.defitrack.event.DefiEventType
import io.defitrack.event.EventDecoder
import io.defitrack.network.toVO
import io.defitrack.protocol.Protocol
import org.springframework.stereotype.Component
import org.web3j.abi.EventEncoder
import org.web3j.abi.FunctionReturnDecoder
import org.web3j.protocol.core.methods.response.Log
import java.math.BigInteger


@Component
class HopTokenEventsDecoder : EventDecoder() {

    val claimEvent = org.web3j.abi.datatypes.Event("Claim", listOf(address(true), uint256()))

    override suspend fun appliesTo(log: Log, network: Network): Boolean {
        return log.address.lowercase() == "0xc5102fe9359fd9a28f877a67e36b0f050d81a3cc"
                && log.topics.map { it.lowercase() }.contains(EventEncoder.encode(claimEvent))
    }

    override suspend fun extract(log: Log, network: Network): DefiEvent {
        val user = getLabeledAddress(
            FunctionReturnDecoder.decodeIndexedValue(
                log.topics[1], address()
            ).value as String
        )

        val amount = FunctionReturnDecoder.decode(
            log.data,
            claimEvent.nonIndexedParameters
        )[0].value as BigInteger

        return DefiEvent(
            transactionId = log.transactionHash,
            type = DefiEventType.CLAIM,
            protocol = Protocol.HOP,
            metadata = mapOf(
                "user" to user,
                "amount" to amount,
                "asset" to getToken("0xc5102fe9359fd9a28f877a67e36b0f050d81a3cc", Network.ETHEREUM)
            ),
            network = network.toVO()
        )
    }

    override fun eventTypes(): List<DefiEventType> {
        return listOf(
            DefiEventType.CLAIM
        )
    }
}