package io.defitrack.events.liquidity

import io.defitrack.abi.TypeUtils
import io.defitrack.abi.TypeUtils.Companion.uint256
import io.defitrack.common.network.Network
import io.defitrack.common.utils.FormatUtilsExtensions.asEth
import io.defitrack.event.DefiEvent
import io.defitrack.event.DefiEventType
import io.defitrack.event.EventDecoder
import io.defitrack.event.EventUtils.Companion.appliesTo
import io.defitrack.evm.contract.BlockchainGatewayProvider
import io.defitrack.network.toVO
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.hop.contract.HopLpTokenContract
import org.springframework.stereotype.Component
import org.web3j.abi.TypeReference
import org.web3j.abi.datatypes.DynamicArray
import org.web3j.abi.datatypes.generated.Uint256
import org.web3j.protocol.core.methods.response.Log
import java.math.BigDecimal
import java.math.BigInteger

@Component
class HopRemoveLiquidityDecoder(
    private val blockchainGatewayProvider: BlockchainGatewayProvider
) : EventDecoder() {

    val removeLiquidityEvent = org.web3j.abi.datatypes.Event(
        "RemoveLiquidity",
        listOf(
            TypeUtils.address(true),
            object : TypeReference<DynamicArray<Uint256>>() {},
            uint256(),
        )
    )


    override suspend fun appliesTo(log: Log, network: Network): Boolean {
        return log.appliesTo(removeLiquidityEvent)
    }

    override suspend fun toDefiEvent(log: Log, network: Network): DefiEvent {

        val contract = HopLpTokenContract(
            blockchainGatewayProvider.getGateway(network), log.address
        )

        val amounts = removeLiquidityEvent.getNonIndexedParameter(log, 0) as List<Uint256>

        val amount1 = amounts[0].value as BigInteger
        val amount2 = amounts[1].value as BigInteger
        val token1 = erC20Resource.getTokenInformation(network, contract.getToken(0))
        val token2 = erC20Resource.getTokenInformation(network, contract.getToken(1))

        return create(
            log = log,
            network = network,
            DefiEventType.REMOVE_LIQUIDITY,
            protocol = Protocol.HOP,
            metadata = mapOf(
                "user" to getLabeledAddress(
                    removeLiquidityEvent.getIndexedParameter<String>(log, 0)
                ),
                "withdrawals" to listOf(
                    mapOf(
                        "token" to token1,
                        "amount" to amount1.asEth(token1.decimals)
                    ),
                    mapOf(
                        "token" to token2,
                        "amount" to amount2.asEth(token2.decimals)
                    )
                ).filter {
                    it["amount"] as BigDecimal > BigDecimal.ZERO
                },
            )
        )
    }

    override fun eventTypes(): List<DefiEventType> {
        return listOf(
            DefiEventType.REMOVE_LIQUIDITY
        )
    }
}