package io.defitrack.events.rewards

import io.defitrack.abi.TypeUtils.Companion.address
import io.defitrack.abi.TypeUtils.Companion.uint256
import io.defitrack.common.network.Network
import io.defitrack.common.utils.AsyncUtils.lazyAsync
import io.defitrack.event.DefiEvent
import io.defitrack.event.DefiEventType
import io.defitrack.event.EventDecoder
import io.defitrack.event.EventUtils.Companion.appliesTo
import io.defitrack.network.toVO
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.hop.HopService
import org.springframework.stereotype.Component
import org.web3j.abi.datatypes.Event
import org.web3j.protocol.core.methods.response.Log
import java.math.BigInteger


@Component
class HopGetRewardsDecoder(
    private val hopService: HopService
) : EventDecoder() {

    val event = Event(
        "RewardPaid",
        listOf(
            address(true),
            uint256()
        )
    )

    override suspend fun appliesTo(log: Log, network: Network): Boolean {
        val pairs = hopService.getStakingRewardsFromJson(network)
        return log.appliesTo(event) && pairs.map {
            it.lowercase()
        }.contains(log.address.lowercase())
    }

    override suspend fun toDefiEvent(log: Log, network: Network): DefiEvent {
        val user = "user" to getLabeledAddress(event.extract<String>(log, true, 0))
        val amount = "amount" to event.extract<BigInteger>(log, false, 0)
        val token = "asset" to getToken("0xc5102fe9359fd9a28f877a67e36b0f050d81a3cc", network)

        return DefiEvent(
            transactionId = log.transactionHash,
            network = network.toVO(),
            protocol = Protocol.HOP,
            type = DefiEventType.GET_REWARD,
            metadata = mapOf(
                user, amount, token
            )
        )
    }

    override fun eventTypes(): List<DefiEventType> {
        return listOf(
            DefiEventType.GET_REWARD
        )
    }
}