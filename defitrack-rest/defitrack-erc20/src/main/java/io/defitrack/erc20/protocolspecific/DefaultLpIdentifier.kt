package io.defitrack.erc20.protocolspecific

import io.defitrack.common.utils.Refreshable
import io.defitrack.erc20.ERC20
import io.defitrack.erc20.LpContractReader
import io.defitrack.erc20.ERC20Service
import io.defitrack.protocol.Protocol
import io.defitrack.token.TokenInformation
import io.defitrack.token.TokenType
import org.springframework.beans.factory.annotation.Autowired

abstract class DefaultLpIdentifier(
    private val protocol: Protocol,
    private val tokenType: TokenType,
    private val lpContractReader: LpContractReader
) : TokenIdentifier {

    @Autowired
    private lateinit var ERC20Service: ERC20Service

    override suspend fun getTokenInfo(token: ERC20): TokenInformation {
        return fromLP(protocol, token, tokenType)
    }

    suspend fun fromLP(protocol: Protocol, erc20: ERC20, tokenType: TokenType): TokenInformation {
        val lp = lpContractReader.getLP(erc20.network, erc20.address)

        val token0 = ERC20Service.getTokenInformation(lp.token0.await(), erc20.network)
        val token1 = ERC20Service.getTokenInformation(lp.token1.await(), erc20.network)

        return TokenInformation(
            name = "${token0.symbol}/${token1.symbol} LP",
            symbol = "${token0.symbol}-${token1.symbol}",
            address = erc20.address,
            decimals = erc20.decimals,
            totalSupply = Refreshable.refreshable(lp.totalSupply()) {
                lp.totalSupply()
            },
            type = tokenType,
            protocol = protocol,
            underlyingTokens = listOf(token0, token1),
            network = erc20.network
        )
    }
}