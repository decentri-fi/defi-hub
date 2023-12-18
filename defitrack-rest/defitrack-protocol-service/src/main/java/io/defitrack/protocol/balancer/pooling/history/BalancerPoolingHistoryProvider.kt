package io.defitrack.protocol.balancer.pooling.history

import arrow.core.nel
import io.defitrack.abi.TypeUtils
import io.defitrack.common.network.Network
import io.defitrack.event.DefiEventType
import io.defitrack.event.EventDecoder.Companion.getNonIndexedParameter
import io.defitrack.market.pooling.PoolingHistoryProvider
import io.defitrack.market.pooling.history.HistoricEventExtractor
import io.defitrack.protocol.Protocol
import io.defitrack.token.ERC20Resource
import org.springframework.stereotype.Component
import org.web3j.abi.TypeEncoder
import org.web3j.abi.TypeReference
import org.web3j.abi.datatypes.Address
import org.web3j.abi.datatypes.DynamicArray
import org.web3j.abi.datatypes.Event
import org.web3j.abi.datatypes.generated.Int256
import org.web3j.abi.datatypes.generated.Uint256
import java.math.BigInteger

@Component
class BalancerPoolingHistoryProvider(
    private val erC20Resource: ERC20Resource,
) : PoolingHistoryProvider() {

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

    fun historicEventExtractor(poolId: String, network: Network): HistoricEventExtractor {
        return HistoricEventExtractor(
            addresses = { "0xba12222222228d8ba445958a75a0704d566bf2c8".nel() },
            optionalTopics = { user ->
                listOf("0x$poolId", "0x${TypeEncoder.encode(Address(user))}")
            },
            topic = "0xe5ce249087ce04f05a957192435400fd97868dba0e6a4b4c049abf8af80dae78",
            toMarketEvent = { event, transaction ->
                val log = event.get()

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

                event(
                    log = log,
                    type = type,
                    protocol = Protocol.BALANCER,
                    network = network,
                    metadata = mapOf(
                        "assets" to tokens.mapIndexed { index, token ->
                            if (deltas[index] == BigInteger.ZERO) {
                                null
                            } else {
                                mapOf(
                                    "token" to erC20Resource.getTokenInformation(network, token),
                                    "amount" to deltas[index].abs().toString()
                                )
                            }
                        }.filterNotNull()
                    )
                )

            }
        )
    }
}