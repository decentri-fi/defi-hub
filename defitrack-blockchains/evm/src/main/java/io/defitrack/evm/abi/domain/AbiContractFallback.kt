package io.defitrack.evm.abi.domain

import io.defitrack.evm.abi.domain.AbiContractElement

data class AbiContractFallback(val stateMutability: String) : AbiContractElement()
