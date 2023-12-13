package io.defitrack.events.swap

import io.defitrack.abi.TypeUtils
import io.defitrack.common.network.Network
import io.defitrack.event.DefiEvent
import io.defitrack.event.DefiEventType
import io.defitrack.event.EventDecoder
import io.defitrack.event.EventUtils.Companion.appliesTo
import io.defitrack.evm.contract.BlockchainGatewayProvider
import io.defitrack.network.toVO
import io.defitrack.uniswap.v3.UniswapV3PoolContract
import org.springframework.stereotype.Component
import org.web3j.abi.datatypes.Event
import org.web3j.protocol.core.methods.response.Log
import java.math.BigInteger

@Component
class SwapEventDecoder(
    private val blockchainGatewayProvider: BlockchainGatewayProvider
) : EventDecoder() {


    val swapEvent = Event(
        "Swap", listOf(
            TypeUtils.address(true),
            TypeUtils.uint256(), //amount0In
            TypeUtils.uint256(), //amount1In
            TypeUtils.uint256(), //amount0Out
            TypeUtils.uint256(), //amount1Out
            TypeUtils.address(true), //to
        )
    )

    override suspend fun appliesTo(log: Log, network: Network): Boolean {
        return log.appliesTo(swapEvent)
    }

    override suspend fun toDefiEvent(log: Log, network: Network): DefiEvent {
        val user = getLabeledAddress(swapEvent.getIndexedParameter<String>(log, 0))
        val amount0In = swapEvent.getNonIndexedParameter<BigInteger>(log, 0)
        val amount1In = swapEvent.getNonIndexedParameter<BigInteger>(log, 1)
        val amount0Out = swapEvent.getNonIndexedParameter<BigInteger>(log, 2)
        val amount1Out = swapEvent.getNonIndexedParameter<BigInteger>(log, 3)

        val gateway = blockchainGatewayProvider.getGateway(network)

        val tokenContract = UniswapV3PoolContract(gateway, log.address)
        val token0 = tokenContract.token0.await()
        val token1 = tokenContract.token1.await()

        val fromToken = if (amount0In > BigInteger.ZERO) {
            token0
        } else {
            token1
        }

        val toToken = if (amount0Out > BigInteger.ZERO) {
            token0
        } else {
            token1
        }

        val fromAmount = if (amount0In > BigInteger.ZERO) {
            amount0In
        } else {
            amount1In
        }

        val toAmount = if (amount0Out > BigInteger.ZERO) {
            amount0Out
        } else {
            amount1Out
        }

        return DefiEvent(
            transactionId = log.transactionHash,
            network = network.toVO(),
            type = DefiEventType.SWAP,
            metadata = mapOf(
                "user" to user,
                "fromToken" to getToken(fromToken, network),
                "toToken" to getToken(toToken, network),
                "fromAmount" to fromAmount,
                "toAmount" to toAmount
            )
        )
    }

    override fun eventTypes(): List<DefiEventType> {
        return listOf(
            DefiEventType.SWAP
        )
    }
}