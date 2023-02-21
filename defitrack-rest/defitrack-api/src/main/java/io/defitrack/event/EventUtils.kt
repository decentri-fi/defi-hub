package io.defitrack.event

import org.web3j.abi.EventEncoder
import org.web3j.abi.datatypes.Event
import org.web3j.protocol.core.methods.response.Log

class EventUtils {

    companion object {
        fun Log.appliesTo(event: Event): Boolean {
            return topics.map { it.lowercase() }.contains(EventEncoder.encode(event))
        }
    }
}