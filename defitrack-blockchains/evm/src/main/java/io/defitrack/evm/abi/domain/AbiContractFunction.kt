package io.defitrack.evm.abi.domain

import com.fasterxml.jackson.annotation.JsonProperty

data class AbiContractFunction(
    val name: String? = null,
    @JsonProperty("payable")
    val isPayable: Boolean = false,
    val stateMutability: String? = null,
    @JsonProperty("constant")
    val isConstant: Boolean = false,
    val inputs: List<AbiElementInput> = emptyList(),
    val outputs: List<AbiElementOutput> = emptyList()
) : AbiContractElement()