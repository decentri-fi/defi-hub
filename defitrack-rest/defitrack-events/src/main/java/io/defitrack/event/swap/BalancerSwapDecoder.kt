package io.defitrack.event.swap

import io.defitrack.abi.TypeUtils
import io.defitrack.common.network.Network
import io.defitrack.event.DefiEvent
import io.defitrack.event.DefiEventType
import io.defitrack.event.EventDecoder
import io.defitrack.event.appliesTo
import io.defitrack.protocol.Protocol
import org.springframework.stereotype.Component
import org.web3j.abi.datatypes.Event
import org.web3j.protocol.core.methods.response.Log
import java.math.BigInteger

@Component
class BalancerSwapDecoder : EventDecoder() {

    val swapEvent = Event(
        "Swap", listOf(
            TypeUtils.bytes32(true),
            TypeUtils.address(true), //amount0In
            TypeUtils.address(true), //amount1In
            TypeUtils.uint256(), //amount0Out
            TypeUtils.uint256(), //amount1Out
        )
    )

    override suspend fun appliesTo(log: Log, network: Network): Boolean {
        return log.address.lowercase() == "0xba12222222228d8ba445958a75a0704d566bf2c8" &&
                log.appliesTo(swapEvent)
    }

    override suspend fun toDefiEvent(log: Log, network: Network): DefiEvent {
        val tokenIn = swapEvent.extract<String>(log, true, 1)
        val tokenOut = swapEvent.extract<String>(log, true, 2)
        val amountIn = swapEvent.extract<BigInteger>(log, false, 0)
        val amountOut = swapEvent.extract<BigInteger>(log, false, 1)

        return create(
            log = log,
            network = network,
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