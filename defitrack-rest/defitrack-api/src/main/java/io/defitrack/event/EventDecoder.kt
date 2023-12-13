package io.defitrack.event

import arrow.core.None
import arrow.core.toOption
import io.defitrack.common.network.Network
import io.defitrack.erc20.FungibleToken
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.BlockchainGatewayProvider
import io.defitrack.labeledaddresses.LabeledAddressesResource
import io.defitrack.labeledaddresses.LabeledAddress
import io.defitrack.token.ERC20Resource
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
    lateinit var erC20Resource: ERC20Resource

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

    suspend fun getToken(address: String, network: Network): FungibleToken {
        return erC20Resource.getTokenInformation(network, address)
    }

    suspend fun getLabeledAddress(address: String): LabeledAddress {
        return labeledAddressesResource.getLabel(address)
    }

    fun getGateway(network: Network): BlockchainGateway {
        return blockchainGatewayProvider.getGateway(network)
    }

    suspend fun createBridgeMetadata(
        token: FungibleToken,
        from: String,
        to: String,
        amount: String
    ): Map<String, Any> {
        return mapOf(
            "assset" to token,
            "from" to getLabeledAddress(from),
            "to" to getLabeledAddress(to),
            "amount" to amount
        )
    }
}