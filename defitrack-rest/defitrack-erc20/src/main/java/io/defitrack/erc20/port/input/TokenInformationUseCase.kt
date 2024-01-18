package io.defitrack.erc20.port.input

import arrow.core.Option
import io.defitrack.common.network.Network
import io.defitrack.erc20.domain.TokenInformation

interface TokenInformationUseCase {
    fun getAllSingleTokens(network: Network, verified: Boolean): List<TokenInformation>
    suspend fun getTokenInformation(
        address: String,
        network: Network,
        verified: Boolean = false
    ): Option<TokenInformation>
}