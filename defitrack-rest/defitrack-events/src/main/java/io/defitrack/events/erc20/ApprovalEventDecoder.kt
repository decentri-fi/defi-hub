package io.defitrack.events.erc20

import io.defitrack.abi.TypeUtils.Companion.address
import io.defitrack.abi.TypeUtils.Companion.uint256
import io.defitrack.common.network.Network
import io.defitrack.event.DefiEvent
import io.defitrack.event.DefiEventType
import io.defitrack.event.EventDecoder
import io.defitrack.event.EventUtils.Companion.appliesTo
import io.defitrack.network.toVO
import org.springframework.stereotype.Component
import org.web3j.abi.FunctionReturnDecoder
import org.web3j.abi.datatypes.Event
import org.web3j.protocol.core.methods.response.Log
import java.math.BigInteger

@Component
class ApprovalEventDecoder : EventDecoder() {

    val event = Event("Approval", listOf(address(true), address(true), uint256()))

    override fun appliesTo(log: Log, network: Network): Boolean {
        return log.appliesTo(event)
    }

    override suspend fun extract(log: Log, network: Network): DefiEvent {
        val owner = "owner" to getLabeledAddress(event.getIndexedParameter<String>(log, 0));

        val spender = "spender" to getLabeledAddress(event.getIndexedParameter<String>(log, 1))

        val amount = "amount" to event.getNonIndexedParameter<BigInteger>(log, 0)

        val asset = "asset" to getToken(log.address, network)

        return DefiEvent(
            transactionId = log.transactionHash,
            type = DefiEventType.APPROVAL,
            metadata = mapOf(owner, spender, asset, amount),
            network = network.toVO()
        )
    }

    override fun eventTypes(): List<DefiEventType> {
        return listOf(DefiEventType.APPROVAL)
    }
}