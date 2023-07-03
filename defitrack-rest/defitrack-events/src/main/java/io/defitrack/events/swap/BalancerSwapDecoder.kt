package io.defitrack.events.swap

import io.defitrack.abi.TypeUtils
import io.defitrack.common.network.Network
import io.defitrack.event.DefiEvent
import io.defitrack.event.DefiEventType
import io.defitrack.event.EventDecoder
import io.defitrack.event.EventUtils.Companion.appliesTo
import io.defitrack.evm.contract.BlockchainGatewayProvider
import io.defitrack.network.toVO
import io.defitrack.protocol.Protocol
import org.springframework.stereotype.Component
import org.web3j.abi.datatypes.Event
import org.web3j.protocol.core.methods.response.Log
import java.math.BigInteger

@Component
class BalancerSwapDecoder(
    private val blockchainGatewayProvider: BlockchainGatewayProvider
) : EventDecoder() {

    val swapEvent = Event(
        "Swap", listOf(
            TypeUtils.bytes32(true),
            TypeUtils.address(true), //amount0In
            TypeUtils.address(true), //amount1In
            TypeUtils.uint256(), //amount0Out
            TypeUtils.uint256(), //amount1Out
        )
    )

    override fun appliesTo(log: Log, network: Network): Boolean {
        return log.address.lowercase() == "0xba12222222228d8ba445958a75a0704d566bf2c8" &&
                log.appliesTo(swapEvent)
    }

    override suspend fun extract(log: Log, network: Network): DefiEvent {
        val tokenIn = swapEvent.getIndexedParameter<String>(log, 1)
        val tokenOut = swapEvent.getIndexedParameter<String>(log, 2)
        val amountIn = swapEvent.getNonIndexedParameter<BigInteger>(log, 0)
        val amountOut = swapEvent.getNonIndexedParameter<BigInteger>(log, 1)

        return DefiEvent(
            transactionId = log.transactionHash,
            network = network.toVO(),
            type = DefiEventType.SWAP,
            protocol = Protocol.BALANCER,
            metadata = mapOf(
                "fromToken" to getToken(tokenIn, network),
                "toToken" to getToken(tokenOut, network),
                "fromAmount" to amountIn,
                "toAmount" to amountOut
            )
        )
    }

    override fun eventTypes(): List<DefiEventType> {
        return listOf(
            DefiEventType.SWAP
        )
    }
}