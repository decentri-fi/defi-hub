package io.defitrack.events.rewards

import io.defitrack.abi.TypeUtils.Companion.address
import io.defitrack.abi.TypeUtils.Companion.uint256
import io.defitrack.common.network.Network
import io.defitrack.event.DefiEvent
import io.defitrack.event.DefiEventType
import io.defitrack.event.EventDecoder
import io.defitrack.event.EventUtils.Companion.appliesTo
import io.defitrack.evm.contract.BlockchainGatewayProvider
import io.defitrack.network.toVO
import io.defitrack.protocol.HopPolygonService
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.contract.HopStakingRewardContract
import org.springframework.stereotype.Component
import org.web3j.abi.datatypes.Event
import org.web3j.protocol.core.methods.response.Log
import java.math.BigInteger


@Component
class HopStakingDecoder(
    hopPolygonService: HopPolygonService,
    private val blockchainGatewayProvider: BlockchainGatewayProvider
) : EventDecoder() {

    val pairMap = mapOf(
        Network.POLYGON to hopPolygonService.getStakingRewards()
    )

    val stakedEvent = Event(
        "Staked",
        listOf(
            address(true),
            uint256()
        )
    )

    override fun appliesTo(log: Log, network: Network): Boolean {
        return log.appliesTo(stakedEvent) && (pairMap[network]?.map {
            it.lowercase()
        }?.contains(log.address.lowercase()) ?: false)
    }

    override suspend fun extract(log: Log, network: Network): DefiEvent {
        val user = "user" to getLabeledAddress(
            stakedEvent.extract<String>(log, true, 0)
        );
        val amount = "amount" to stakedEvent.extract<BigInteger>(log, false, 0)

        val contract = HopStakingRewardContract(
            blockchainGatewayProvider.getGateway(network),
            "", log.address
        )

        val token = "token" to getToken(contract.stakingTokenAddress(), network).toFungibleToken()

        return DefiEvent(
            transactionId = log.transactionHash,
            network = network.toVO(),
            protocol = Protocol.HOP,
            type = DefiEventType.STAKE,
            metadata = mapOf(
                user, amount, token
            )
        )
    }

    override fun eventTypes(): List<DefiEventType> {
        return listOf(
            DefiEventType.STAKE
        )
    }
}