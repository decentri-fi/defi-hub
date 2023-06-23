package io.defitrack.event

import io.defitrack.common.network.Network
import io.defitrack.erc20.TokenInformationVO
import io.defitrack.labeledaddresses.LabelAddressesResource
import io.defitrack.labeledaddresses.LabeledAddress
import io.defitrack.token.ERC20Resource
import org.springframework.beans.factory.annotation.Autowired
import org.web3j.abi.FunctionReturnDecoder
import org.web3j.protocol.core.methods.response.Log

abstract class EventDecoder {

    companion object {
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
    lateinit var labelAddressesResource: LabelAddressesResource

    abstract fun appliesTo(log: Log, network: Network): Boolean
    abstract suspend fun extract(log: Log, network: Network): DefiEvent

    abstract fun eventTypes(): List<DefiEventType>

    suspend fun getToken(address: String, network: Network): TokenInformationVO {
        return erC20Resource.getTokenInformation(network, address)
    }





    suspend fun getLabeledAddress(address: String): LabeledAddress {
        return labelAddressesResource.getLabel(address)
    }
}