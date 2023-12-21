package io.defitrack.events.rewards

import io.defitrack.abi.TypeUtils.Companion.address
import io.defitrack.abi.TypeUtils.Companion.uint256
import io.defitrack.common.network.Network
import io.defitrack.event.DefiEvent
import io.defitrack.event.DefiEventType
import io.defitrack.event.EventDecoder
import io.defitrack.event.appliesTo
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.velodrome.contract.VelodromeV2GaugeContract
import org.springframework.stereotype.Component
import org.web3j.abi.datatypes.Event
import org.web3j.protocol.core.methods.response.Log
import java.math.BigInteger


@Component
class ClaimRewardsEventDecoder : EventDecoder() {

    val claimRewardsEvent = Event(
        "ClaimRewards",
        listOf(
            address(true),
            uint256()
        )
    )

    override suspend fun appliesTo(log: Log, network: Network): Boolean {
        return log.appliesTo(claimRewardsEvent) &&
                network == Network.OPTIMISM
    }

    override suspend fun toDefiEvent(log: Log, network: Network): DefiEvent {
        val user = "user" to getLabeledAddress(
            claimRewardsEvent.extract<String>(log, true, 0)
        )

        val amount = "amount" to claimRewardsEvent.extract<BigInteger>(log, false, 0)

        val rewardToken = getToken(
            VelodromeV2GaugeContract(
                getGateway(network), log.address
            ).rewardToken.await(), network
        )

        val token = "asset" to rewardToken

        return create(
            log = log,
            network = network,
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