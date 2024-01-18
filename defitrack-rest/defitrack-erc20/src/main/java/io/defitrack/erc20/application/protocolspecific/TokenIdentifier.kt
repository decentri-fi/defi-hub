package io.defitrack.erc20.application.protocolspecific

import io.defitrack.erc20.ERC20
import io.defitrack.erc20.application.ERC20TokenService
import io.defitrack.erc20.domain.TokenInformation
import org.springframework.beans.factory.annotation.Autowired

abstract class TokenIdentifier {

    @Autowired
    protected lateinit var erc20TokenService: ERC20TokenService

    abstract suspend fun isProtocolToken(token: ERC20): Boolean
    abstract suspend fun getTokenInfo(token: ERC20): TokenInformation

}