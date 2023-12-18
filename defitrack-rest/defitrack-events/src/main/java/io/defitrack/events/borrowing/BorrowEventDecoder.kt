package io.defitrack.events.borrowing

import io.defitrack.abi.TypeUtils.Companion.address
import io.defitrack.abi.TypeUtils.Companion.uint256
import io.defitrack.common.network.Network
import io.defitrack.event.DefiEvent
import io.defitrack.event.DefiEventType
import io.defitrack.event.EventDecoder
import io.defitrack.event.EventUtils.Companion.appliesTo
import io.defitrack.network.toVO
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

    override suspend fun appliesTo(log: Log, network: Network): Boolean {
        return log.appliesTo(event)
    }

    override suspend fun toDefiEvent(log: Log, network: Network): DefiEvent {
        val borrower = "borrower" to getLabeledAddress(event.extract<String>(log, false, 0))
        val borrowAmount = "borrowAmount" to event.extract<BigInteger>(log, false, 1)
        val asset = "asset" to getToken(log.address, network)

        return create(
            log = log,
            type = DefiEventType.BORROW,
            metadata = mapOf(borrower, borrowAmount, asset),
            network = network
        )
    }

    override fun eventTypes(): List<DefiEventType> {
        return listOf(
            DefiEventType.BORROW
        )
    }
}