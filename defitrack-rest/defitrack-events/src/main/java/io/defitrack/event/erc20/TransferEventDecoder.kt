package io.defitrack.event.erc20

import io.defitrack.abi.TypeUtils.Companion.address
import io.defitrack.abi.TypeUtils.Companion.uint256
import io.defitrack.common.network.Network
import io.defitrack.event.DefiEvent
import io.defitrack.event.DefiEventType
import io.defitrack.event.EventDecoder
import io.defitrack.event.appliesTo
import org.springframework.stereotype.Component
import org.web3j.abi.datatypes.Event
import org.web3j.protocol.core.methods.response.Log
import java.math.BigInteger

@Component
class TransferEventDecoder : EventDecoder() {

    val event = Event("Transfer", listOf(address(true), address(true), uint256()))

    override suspend fun appliesTo(log: Log, network: Network): Boolean {
        return log.appliesTo(event)
    }

    override suspend fun toDefiEvent(log: Log, network: Network): DefiEvent {
        val from = "from" to getLabeledAddress(event.extract<String>(log, true, 0))
        val to = "to" to getLabeledAddress(event.extract<String>(log, true, 1))
        val amount = "amount" to event.extract<BigInteger>(log, false, 0)
        val asset = "asset" to getToken(log.address, network)

        if (to.second.address == "0x0000000000000000000000000000000000000000") {
            create(
                log = log,
                type = DefiEventType.BURN,
                metadata = mapOf(from, asset, amount),
                network = network
            )
        } else if (from.second.address == "0x0000000000000000000000000000000000000000") {
            return create(
                log = log,
                type = DefiEventType.MINT,
                metadata = mapOf(from, asset, amount),
                network = network
            )
        }

        return create(
            log = log,
            type = DefiEventType.TRANSFER,
            metadata = mapOf(from, to, asset, amount),
            network = network
        )
    }

    override fun eventTypes(): List<DefiEventType> {
        return listOf(
            DefiEventType.TRANSFER, DefiEventType.BURN, DefiEventType.MINT
        )
    }
}