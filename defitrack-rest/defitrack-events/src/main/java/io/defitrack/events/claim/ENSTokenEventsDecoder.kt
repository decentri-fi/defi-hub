package io.defitrack.events.claim

import io.defitrack.abi.TypeUtils.Companion.address
import io.defitrack.abi.TypeUtils.Companion.uint256
import io.defitrack.common.network.Network
import io.defitrack.event.DefiEvent
import io.defitrack.event.DefiEventType
import io.defitrack.event.EventDecoder
import org.springframework.stereotype.Component
import org.web3j.abi.EventEncoder
import org.web3j.protocol.core.methods.response.Log
import java.math.BigInteger


@Component
class ENSTokenEventsDecoder : EventDecoder() {

    val claimEvent = org.web3j.abi.datatypes.Event("Claim", listOf(address(true), uint256()))

    override fun appliesTo(log: Log, network: Network): Boolean {
        return log.address.lowercase() == "0xc18360217d8f7ab5e7c516566761ea12ce7f9d72"
                && log.topics.map { it.lowercase() }.contains(EventEncoder.encode(claimEvent))
    }

    override suspend fun extract(log: Log, network: Network): DefiEvent {
        val user = getLabeledAddress(
            claimEvent.getIndexedParameter<String>(log, 0)
        )

        val amount = claimEvent.getNonIndexedParameter<BigInteger>(log, 0)

        return DefiEvent(
            type = DefiEventType.CLAIM,
            metadata = mapOf(
                "user" to user,
                "amount" to amount,
                "asset" to getToken("0xc18360217d8f7ab5e7c516566761ea12ce7f9d72", Network.ETHEREUM)
            )
        )
    }

    override fun eventTypes(): List<DefiEventType> {
        return listOf(
            DefiEventType.CLAIM
        )
    }
}