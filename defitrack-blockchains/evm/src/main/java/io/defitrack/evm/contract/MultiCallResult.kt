package io.defitrack.evm.contract

import org.web3j.abi.datatypes.Type

data class MultiCallResult(
    val success: Boolean,
    val data: List<Type<*>>
)