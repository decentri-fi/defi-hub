package io.defitrack.erc20.protocolspecific

import io.defitrack.erc20.ERC20
import io.defitrack.token.TokenInformation

interface TokenIdentifier {

    suspend fun isProtocolToken(token: ERC20): Boolean
    suspend fun getTokenInfo(token: ERC20): TokenInformation

}