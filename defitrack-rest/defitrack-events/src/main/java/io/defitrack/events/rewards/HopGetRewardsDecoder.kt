package io.defitrack.events.rewards

import io.defitrack.abi.TypeUtils.Companion.address
import io.defitrack.abi.TypeUtils.Companion.uint256
import io.defitrack.common.network.Network
import io.defitrack.event.DefiEvent
import io.defitrack.event.DefiEventType
import io.defitrack.event.EventDecoder
import io.defitrack.event.EventUtils.Companion.appliesTo
import io.defitrack.network.toVO
import io.defitrack.protocol.HopPolygonService
import io.defitrack.protocol.Protocol
import org.springframework.stereotype.Component
import org.web3j.protocol.core.methods.response.Log
import java.math.BigInteger


@Component
class HopGetRewardsDecoder(
    hopPolygonService: HopPolygonService
) : EventDecoder() {

    val pairMap = mapOf(
        Network.POLYGON to hopPolygonService.getStakingRewards()
    )

    val rewardPairEvent = org.web3j.abi.datatypes.Event(
        "RewardPaid",
        listOf(
            address(true),
            uint256()
        )
    )

    override fun appliesTo(log: Log, network: Network): Boolean {
        return log.appliesTo(rewardPairEvent) && (pairMap[network]?.map {
            it.lowercase()
        }?.contains(log.address.lowercase()) ?: false)
    }

    override suspend fun extract(log: Log, network: Network): DefiEvent {
        val user = "user" to getLabeledAddress(
            rewardPairEvent.getIndexedParameter<String>(log, 0)
        );
        val amount = "amount" to rewardPairEvent.getNonIndexedParameter<BigInteger>(log, 0)

        val token = "asset" to erC20Resource.getTokenInformation(network, "0xc5102fe9359fd9a28f877a67e36b0f050d81a3cc")

        return DefiEvent(
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