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
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.contract.HopLpTokenContract
import org.springframework.stereotype.Component
import org.web3j.abi.TypeReference
import org.web3j.abi.datatypes.DynamicArray
import org.web3j.abi.datatypes.generated.Uint256
import org.web3j.protocol.core.methods.response.Log
import java.math.BigDecimal
import java.math.BigInteger

@Component
class HopAddLiquidityDecoder(
    private val blockchainGatewayProvider: BlockchainGatewayProvider
) : EventDecoder() {

    val addLiquidityEvent = org.web3j.abi.datatypes.Event(
        "AddLiquidity",
        listOf(
            TypeUtils.address(true),
            object : TypeReference<DynamicArray<Uint256>>() {},
            object : TypeReference<DynamicArray<Uint256>>() {},
            uint256(),
            uint256(),
        )
    )


    override fun appliesTo(log: Log, network: Network): Boolean {
        return log.appliesTo(addLiquidityEvent)
    }

    override suspend fun extract(log: Log, network: Network): DefiEvent {

        val contract = HopLpTokenContract(
            blockchainGatewayProvider.getGateway(network),
            "", log.address
        )

        val amounts = addLiquidityEvent.getNonIndexedParameter(log, 0) as List<Uint256>

        val amount1 = amounts[0].value as BigInteger
        val amount2 = amounts[1].value as BigInteger
        val token1 = erC20Resource.getTokenInformation(network, contract.getToken(0))
        val token2 = erC20Resource.getTokenInformation(network, contract.getToken(1))

        return DefiEvent(
            DefiEventType.ADD_LIQUIDITY,
            protocol = Protocol.HOP,
            metadata = mapOf(
                "user" to addLiquidityEvent.getIndexedParameter<String>(log, 0),
                "deposits" to listOf(
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
}