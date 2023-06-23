package io.defitrack.protocol.balancer.pooling.history

import io.defitrack.abi.TypeUtils
import io.defitrack.event.DefiEvent
import io.defitrack.event.DefiEventType
import io.defitrack.event.EventDecoder.Companion.getIndexedParameter
import io.defitrack.event.EventDecoder.Companion.getNonIndexedParameter
import io.defitrack.market.pooling.history.HistoricEventExtractor
import io.defitrack.market.pooling.history.PoolingHistoryProvider
import io.defitrack.network.toVO
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.balancer.pooling.BalancerPoolingMarketProvider
import org.apache.commons.codec.binary.Hex
import org.web3j.abi.TypeEncoder
import org.web3j.abi.TypeReference
import org.web3j.abi.datatypes.Address
import org.web3j.abi.datatypes.DynamicArray
import org.web3j.abi.datatypes.Event
import org.web3j.abi.datatypes.generated.Int256
import org.web3j.abi.datatypes.generated.Uint256
import java.math.BigInteger

abstract class BalancerPoolingHistoryProvider(
    balancerPoolingMarketProvider: BalancerPoolingMarketProvider
) : PoolingHistoryProvider(balancerPoolingMarketProvider) {

    val PoolBalanceChangedEvent = Event(
        "PoolBalanceChanged",
        listOf(
            TypeUtils.bytes32(true),
            TypeUtils.address(true),
            object : TypeReference<DynamicArray<Address>>(false) {},
            object : TypeReference<DynamicArray<Int256>>(false) {},
            object : TypeReference<DynamicArray<Uint256>>(false) {},
        )
    )

    override fun historicEventExtractor(): HistoricEventExtractor {
        val allMarkets = ArrayList(
            poolingMarketProvider.getMarkets()
        )

        return HistoricEventExtractor(
            addresses = {
                listOf("0xba12222222228d8ba445958a75a0704d566bf2c8")
            },
            optionalTopics = { user ->
                listOf(null, "0x${TypeEncoder.encode(Address(user))}")
            },
            topic = "0xe5ce249087ce04f05a957192435400fd97868dba0e6a4b4c049abf8af80dae78",
            toMarketEvent = { event ->
                val log = event.get()

                val poolId = Hex.encodeHexString(
                    PoolBalanceChangedEvent.getIndexedParameter<ByteArray>(
                        log, 0
                    )
                );

                val market = (allMarkets.find {
                    it.metadata["poolId"] == poolId
                })

                if (market == null) {
                    null
                } else {
                    val deltas = PoolBalanceChangedEvent.getNonIndexedParameter<List<Int256>>(
                        log, 1
                    ).map {
                        it.value as BigInteger
                    }

                    val tokens = PoolBalanceChangedEvent.getNonIndexedParameter<List<Address>>(
                        log, 0
                    ).map {
                        it.value as String
                    }

                    val type = if (deltas.none { it < BigInteger.ZERO }) {
                        DefiEventType.ADD_LIQUIDITY
                    } else {
                        DefiEventType.REMOVE_LIQUIDITY
                    }

                    DefiEvent(
                        type = type,
                        protocol = Protocol.BALANCER,
                        network = getNetwork().toVO(),
                        metadata = mapOf(
                            "market" to (allMarkets.find {
                                it.metadata["poolId"] == poolId
                            }?.id ?: "unknown"),
                            "assets" to tokens.mapIndexed { index, token ->
                                if (deltas[index] == BigInteger.ZERO) {
                                    null
                                } else {
                                    mapOf(
                                        "token" to erC20Resource.getTokenInformation(getNetwork(), token),
                                        "amount" to deltas[index].abs().toString()
                                    )
                                }
                            }.filterNotNull()
                        )
                    )
                }
            }
        )
    }

}