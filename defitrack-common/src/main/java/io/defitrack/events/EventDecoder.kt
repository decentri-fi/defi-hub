package io.defitrack.events

import org.web3j.protocol.core.methods.response.Log

interface EventDecoder {
    fun appliesTo(log: Log): Boolean
    fun extract(log: Log): DefiEvent
}