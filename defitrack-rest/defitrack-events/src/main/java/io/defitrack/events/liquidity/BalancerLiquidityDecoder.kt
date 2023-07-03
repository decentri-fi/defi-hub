package io.defitrack.events.liquidity

import io.defitrack.abi.TypeUtils
import io.defitrack.common.network.Network
import io.defitrack.event.DefiEvent
import io.defitrack.event.DefiEventType
import io.defitrack.event.EventDecoder
import io.defitrack.event.EventUtils.Companion.appliesTo
import io.defitrack.network.toVO
import io.defitrack.protocol.Protocol
import org.apache.commons.codec.binary.Hex
import org.springframework.stereotype.Component
import org.web3j.abi.TypeReference
import org.web3j.abi.datatypes.Address
import org.web3j.abi.datatypes.DynamicArray
import org.web3j.abi.datatypes.Event
import org.web3j.abi.datatypes.generated.Int256
import org.web3j.abi.datatypes.generated.Uint256
import org.web3j.protocol.core.methods.response.Log
import java.math.BigInteger

@Component
class BalancerLiquidityDecoder : EventDecoder() {

    val poolBalanceChanged = Event(
        "PoolBalanceChanged",
        listOf(
            TypeUtils.bytes32(true),
            TypeUtils.address(true),
            object : TypeReference<DynamicArray<Address>>(false) {},
            object : TypeReference<DynamicArray<Int256>>(false) {},
            object : TypeReference<DynamicArray<Uint256>>(false) {},
        )
    )

    override fun appliesTo(log: Log, network: Network): Boolean {
        return log.appliesTo(poolBalanceChanged)
    }

    override suspend fun extract(log: Log, network: Network): DefiEvent {
        val poolId = Hex.encodeHexString(
            poolBalanceChanged.getIndexedParameter<ByteArray>(
                log, 0
            )
        );

        val deltas = poolBalanceChanged.getNonIndexedParameter<List<Int256>>(
            log, 1
        ).map {
            it.value as BigInteger
        }

        val tokens = poolBalanceChanged.getNonIndexedParameter<List<Address>>(
            log, 0
        ).map {
            it.value as String
        }

        val type = if (deltas.none { it < BigInteger.ZERO }) {
            DefiEventType.ADD_LIQUIDITY
        } else {
            DefiEventType.REMOVE_LIQUIDITY
        }

        return DefiEvent(
            transactionId = log.transactionHash,
            type = type,
            protocol = Protocol.BALANCER,
            network = network.toVO(),
            metadata = mapOf(
                /*           "market" to mapOf(
                               "id" to (allMarkets.find {
                                   it.metadata["poolId"] == poolId
                               }?.id ?: "unknown"),
                               "type" to "pooling"
                           ), */
                "assets" to tokens.mapIndexed { index, token ->
                    if (deltas[index] == BigInteger.ZERO) {
                        null
                    } else {
                        mapOf(
                            "token" to getToken(token, network),
                            "amount" to deltas[index].abs().toString()
                        )
                    }
                }.filterNotNull()
            )
        )
    }

    override fun eventTypes(): List<DefiEventType> {
        return listOf(
            DefiEventType.ADD_LIQUIDITY,
            DefiEventType.REMOVE_LIQUIDITY
        )
    }
}