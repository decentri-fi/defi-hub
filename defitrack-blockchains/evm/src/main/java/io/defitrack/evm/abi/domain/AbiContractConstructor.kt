package io.defitrack.abi.domain

data class AbiContractConstructor(val isPayable: Boolean = false,
                                  val stateMutability: String? = null,
                                  val inputs: List<AbiElementInput>? = null) : AbiContractElement()
