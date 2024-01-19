package io.defitrack.adapter.output.resource

import io.defitrack.domain.WrappedToken

internal data class WrappedTokenResponse(val address: String) {
    fun toWrappedToken(): WrappedToken {
        return WrappedToken(address = address)
    }
}