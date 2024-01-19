package io.defitrack.adapter.output.resource

import io.defitrack.token.TokenType

internal enum class TokenTypeResponse {
    SINGLE, STANDARD_LP, CUSTOM_LP, NATIVE, OTHER;

    fun toTokenType(): TokenType {
        return TokenType.valueOf(name)
    }
}