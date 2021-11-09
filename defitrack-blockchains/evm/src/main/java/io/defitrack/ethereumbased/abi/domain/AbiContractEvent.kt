package io.defitrack.abi.domain

data class AbiContractEvent(val anonymous: Boolean = false,
                            val name: String,
                            val inputs: List<AbiElementEventInput> = emptyList()) : AbiContractElement()
