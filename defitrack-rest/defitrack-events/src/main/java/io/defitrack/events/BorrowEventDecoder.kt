package io.defitrack.events

import io.defitrack.abi.TypeUtils.Companion.address
import io.defitrack.abi.TypeUtils.Companion.uint256
import io.defitrack.common.network.Network
import io.defitrack.event.DefiEvent
import io.defitrack.event.DefiEventType
import io.defitrack.event.EventDecoder
import io.defitrack.event.EventUtils.Companion.appliesTo
import org.springframework.stereotype.Component
import org.web3j.abi.datatypes.Event
import org.web3j.protocol.core.methods.response.Log
import java.math.BigInteger

@Component
class BorrowEventDecoder : EventDecoder() {

    val event = Event(
        "Borrow",
        listOf(
            address(),
            uint256(),
            uint256(),
            uint256(),
        )
    )

    override fun appliesTo(log: Log): Boolean {
        return log.appliesTo(event)
    }

    override suspend fun extract(log: Log, network: Network): DefiEvent {
        val borrower =
            "borrower" to event.getNonIndexedParameter<String>(log, 0)

        val borrowAmount = "borrowAmount" to event.getNonIndexedParameter<BigInteger>(log, 1)
        val asset = "asset" to getToken(log.address, network)

        return DefiEvent(
            type = DefiEventType.BORROW,
            metadata = mapOf(borrower, borrowAmount, asset)
        )
    }
}