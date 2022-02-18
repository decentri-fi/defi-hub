package io.defitrack.evm.abi.domain

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes(
        JsonSubTypes.Type(value = AbiContractFunction::class, name = "function"),
        JsonSubTypes.Type(value = AbiContractConstructor::class, name = "constructor"),
        JsonSubTypes.Type(value = AbiContractFallback::class, name = "fallback"),
        JsonSubTypes.Type(value = AbiContractReceive::class, name = "receive"),
        JsonSubTypes.Type(value = AbiContractEvent::class, name = "event")
)
open class AbiContractElement
