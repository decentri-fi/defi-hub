package io.defitrack.erc20.protocolspecific

import io.defitrack.erc20.ERC20
import io.defitrack.erc20.ERC20Service
import io.defitrack.erc20.TokenInformation
import org.springframework.beans.factory.annotation.Autowired

abstract class TokenIdentifier {

    @Autowired
    protected lateinit var erc20Service: ERC20Service

    abstract suspend fun isProtocolToken(token: ERC20): Boolean
    abstract suspend fun getTokenInfo(token: ERC20): TokenInformation

}