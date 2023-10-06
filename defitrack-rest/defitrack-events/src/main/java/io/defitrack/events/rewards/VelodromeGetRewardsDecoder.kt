package io.defitrack.events.rewards

import io.defitrack.abi.TypeUtils.Companion.address
import io.defitrack.abi.TypeUtils.Companion.uint256
import io.defitrack.common.network.Network
import io.defitrack.event.DefiEvent
import io.defitrack.event.DefiEventType
import io.defitrack.event.EventDecoder
import io.defitrack.event.EventUtils.Companion.appliesTo
import io.defitrack.network.toVO
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.velodrome.contract.VelodromeV2GaugeContract
import org.springframework.stereotype.Component
import org.web3j.protocol.core.methods.response.Log
import java.math.BigInteger


@Component
class VelodromeGetRewardsDecoder(
) : EventDecoder() {

    val rewardPairEvent = org.web3j.abi.datatypes.Event(
        "ClaimRewards",
        listOf(
            address(true),
            uint256()
        )
    )

    override suspend fun appliesTo(log: Log, network: Network): Boolean {
        return log.appliesTo(rewardPairEvent) &&
                network == Network.OPTIMISM
    }

    override suspend fun extract(log: Log, network: Network): DefiEvent {
        val user = "user" to getLabeledAddress(
            rewardPairEvent.extract<String>(log, true, 0)
        )

        val amount = "amount" to rewardPairEvent.extract<BigInteger>(log, false, 0)

        val rewardToken = getToken(
            VelodromeV2GaugeContract(
                getGateway(network), log.address
            ).rewardToken.await(), network
        )

        val token = "asset" to rewardToken

        return DefiEvent(
            transactionId = log.transactionHash,
            network = network.toVO(),
            protocol = Protocol.VELODROME_V2,
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