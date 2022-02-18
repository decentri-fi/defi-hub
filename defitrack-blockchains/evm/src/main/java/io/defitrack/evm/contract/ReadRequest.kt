package io.defitrack.evm.contract

import org.web3j.abi.TypeReference
import org.web3j.abi.datatypes.Type

data class ReadRequest(
    val method: String,
    val inputs: List<Type<*>> = emptyList(),
    val outputs: List<TypeReference<out Type<*>>?>? = null
)