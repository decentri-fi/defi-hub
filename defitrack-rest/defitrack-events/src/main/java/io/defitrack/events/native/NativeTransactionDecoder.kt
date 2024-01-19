package io.defitrack.events.native

import arrow.core.nel
import io.defitrack.common.network.Network
import io.defitrack.domain.toNetworkInformation
import io.defitrack.event.DefiEvent
import io.defitrack.event.DefiEventType
import io.defitrack.evm.contract.BlockchainGatewayProvider
import io.defitrack.labeledaddresses.LabeledAddressesResource
import io.defitrack.port.input.ERC20Resource
import org.springframework.stereotype.Component
import java.math.BigInteger

@Component
class NativeTransactionDecoder(
    private val blockchainGatewayProvider: BlockchainGatewayProvider,
    private val labeledAddressesResource: LabeledAddressesResource,
    private val erC20Resource: ERC20Resource
) {

    suspend fun extract(txHash: String, network: Network): List<DefiEvent> {
        val provider = blockchainGatewayProvider.getGateway(network)

        return provider.getTransaction(txHash)?.takeIf { it.value > BigInteger.ZERO }?.run {

            val from = labeledAddressesResource.getLabel(from)
            val to = labeledAddressesResource.getLabel(to ?: "")
            val asset = erC20Resource.getTokenInformation(network, "0x0")

            val transaction = provider.getTransaction(txHash)!!
            val id = network.slug + "_" + transaction.hash

            DefiEvent(
                id = id,
                transaction = transaction,
                type = DefiEventType.TRANSFER,
                metadata = mapOf(
                    "from" to from,
                    "to" to to,
                    "value" to value,
                    "asset" to asset
                ),
                network = network.toNetworkInformation()
            ).nel()
        } ?: emptyList()
    }
}