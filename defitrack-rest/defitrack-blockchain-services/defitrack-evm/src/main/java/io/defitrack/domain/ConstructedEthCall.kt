package io.defitrack.domain

import org.web3j.protocol.core.methods.response.EthCall

class ConstructedEthCall(val success: Boolean, val data: String?) : EthCall() {

    constructor(success: Boolean) : this(success, null)

    override fun getId(): Long {
        return 0L
    }

    override fun getJsonrpc(): String {
        return "2.0"
    }

    override fun getResult(): String {
        return data ?: "0x"
    }

    override fun getError(): Error? {
        return super.getError()
    }

    override fun getValue(): String {
        return data ?: "0x"
    }

    override fun isReverted(): Boolean {
        return !success
    }

    override fun getRevertReason(): String? {
        return super.getRevertReason()
    }
}