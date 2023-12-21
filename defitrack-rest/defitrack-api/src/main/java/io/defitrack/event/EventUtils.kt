package io.defitrack.event

import org.web3j.abi.EventEncoder
import org.web3j.abi.datatypes.Event
import org.web3j.protocol.core.methods.response.Log

fun Log.appliesTo(event: Event): Boolean {
    return topics.map { it.lowercase() }.contains(EventEncoder.encode(event))
            && topics.size == event.indexedParameters.size + 1
}

fun Event.getEncodedTopic(): String {
    return EventEncoder.encode(this)
}