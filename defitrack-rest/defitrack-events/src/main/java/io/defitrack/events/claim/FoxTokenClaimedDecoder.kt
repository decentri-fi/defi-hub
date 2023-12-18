package io.defitrack.events.claim

import io.defitrack.abi.TypeUtils
import io.defitrack.common.network.Network
import io.defitrack.event.DefiEvent
import io.defitrack.event.DefiEventType
import io.defitrack.event.EventDecoder
import io.defitrack.event.EventUtils.Companion.appliesTo
import io.defitrack.network.toVO
import org.springframework.stereotype.Component
import org.web3j.protocol.core.methods.response.Log
import java.math.BigInteger

@Component
class FoxTokenClaimedDecoder : EventDecoder() {

    val claimedEvent = org.web3j.abi.datatypes.Event(
        "Claimed",
        listOf(
            TypeUtils.uint256(),
            TypeUtils.address(true),
            TypeUtils.uint256(),
            TypeUtils.uint256(),
            TypeUtils.uint256()
        )
    )

    override suspend fun appliesTo(log: Log, network: Network): Boolean {
        return log.address == "0xb90381dae1a72528660278100c5aa44e1108cef7" && log.appliesTo(claimedEvent)
    }

    override suspend fun toDefiEvent(log: Log, network: Network): DefiEvent {
        val user = "user" to getLabeledAddress(
            claimedEvent.getIndexedParameter<String>(log, 0)
        )

        val amount = "amount" to claimedEvent.getNonIndexedParameter<BigInteger>(log, 1)

        val asset = "asset" to getToken("0xc770eefad204b5180df6a14ee197d99d808ee52d", network)

        return DefiEvent(
            transaction = getTransaction(network, log.transactionHash),
            type = DefiEventType.CLAIM,
            metadata = mapOf(user, asset, amount),
            network = network.toVO()
        )
    }

    override fun eventTypes(): List<DefiEventType> {
        return listOf(
            DefiEventType.CLAIM
        )
    }
}