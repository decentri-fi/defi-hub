package io.defitrack.evm.abi.domain

import com.fasterxml.jackson.annotation.JsonProperty
import io.defitrack.evm.abi.domain.AbiContractElement

data class AbiContractReceive(val stateMutability: String,
                              @JsonProperty("payable")
                               val isPayable: Boolean = false) : AbiContractElement()
