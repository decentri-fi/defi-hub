package io.defitrack.event

import arrow.core.None
import arrow.core.toOption
import io.defitrack.adapter.output.domain.erc20.FungibleTokenInformation
import io.defitrack.common.network.Network
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.BlockchainGatewayProvider
import io.defitrack.labeledaddress.adapter.out.LabeledAddressesResource
import io.defitrack.labeledaddress.domain.LabeledAddress
import io.defitrack.port.output.ERC20Client
import io.defitrack.protocol.Protocol
import org.springframework.beans.factory.annotation.Autowired
import org.web3j.abi.FunctionReturnDecoder
import org.web3j.protocol.core.methods.response.Log

abstract class EventDecoder {

    @Autowired
    private lateinit var blockchainGatewayProvider: BlockchainGatewayProvider

    companion object {

        inline fun <reified T> org.web3j.abi.datatypes.Event.extract(log: Log, indexed: Boolean, index: Int): T {
            return if (indexed) {
                getIndexedParameter(log, index)
            } else {
                getNonIndexedParameter(log, index)
            }
        }

        inline fun <reified T> org.web3j.abi.datatypes.Event.getNonIndexedParameter(log: Log, index: Int): T {
            return FunctionReturnDecoder.decode(
                log.data,
                nonIndexedParameters
            )[index].value as T
        }

        inline fun <reified T> org.web3j.abi.datatypes.Event.getIndexedParameter(log: Log, index: Int): T {
            return FunctionReturnDecoder.decodeIndexedValue(
                log.topics[index + 1], indexedParameters[index]
            ).value as T
        }
    }

    @Autowired
    lateinit var erC20Resource: ERC20Client

    @Autowired
    lateinit var labeledAddressesResource: LabeledAddressesResource

    protected abstract suspend fun appliesTo(log: Log, network: Network): Boolean

    suspend fun extract(log: Log, network: Network): arrow.core.Option<DefiEvent> {
        return if (appliesTo(log, network)) {
            return toDefiEvent(log, network).toOption()
        } else {
            None
        }
    }

    abstract suspend fun toDefiEvent(log: Log, network: Network): DefiEvent

    abstract fun eventTypes(): List<DefiEventType>

    suspend fun getToken(address: String, network: Network): FungibleTokenInformation {
        return erC20Resource.getTokenInformation(network, address)
    }

    suspend fun getLabeledAddress(address: String): LabeledAddress {
        return labeledAddressesResource.getLabel(address)
    }

    fun getGateway(network: Network): BlockchainGateway {
        return blockchainGatewayProvider.getGateway(network)
    }

    suspend fun getTransaction(network: Network, txId: String): BlockchainGateway.TransactionVO {
        return getGateway(network).getTransaction(txId)
            ?: throw IllegalArgumentException("Invalid transaction $txId for network $network")
    }


    suspend fun create(
        log: Log,
        network: Network,
        type: DefiEventType,
        protocol: Protocol? = null,
        metadata: Map<String, Any> = emptyMap()
    ): DefiEvent {

        val transaction = getTransaction(network, log.transactionHash)
        val id = network.slug + "_" + transaction.hash + "-" + log.logIndex

        return DefiEvent(
            id = id,
            protocol = protocol,
            transaction = getTransaction(network, log.transactionHash),
            type = type,
            metadata = metadata,
            network = network
        )
    }
}