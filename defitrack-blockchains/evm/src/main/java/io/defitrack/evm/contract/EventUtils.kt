package io.defitrack.evm.contract

import org.web3j.abi.FunctionReturnDecoder
import org.web3j.protocol.core.methods.response.Log

object EventUtils {
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