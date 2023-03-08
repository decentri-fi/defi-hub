package io.defitrack.erc20.protocolspecific

import io.defitrack.erc20.ERC20
import io.defitrack.erc20.LpContractReader
import io.defitrack.erc20.TokenService
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
    private lateinit var tokenService: TokenService

    override suspend fun getTokenInfo(token: ERC20): TokenInformation {
        return fromLP(protocol, token, tokenType)
    }

    suspend fun fromLP(protocol: Protocol, erc20: ERC20, tokenType: TokenType): TokenInformation {
        val lp = lpContractReader.getLP(erc20.network, erc20.address)

        val token0 = tokenService.getTokenInformation(lp.token0(), erc20.network)
        val token1 = tokenService.getTokenInformation(lp.token1(), erc20.network)

        return TokenInformation(
            name = "${token0.symbol}/${token1.symbol} LP",
            symbol = "${token0.symbol}-${token1.symbol}",
            address = erc20.address,
            decimals = erc20.decimals,
            totalSupply = lp.totalSupply(),
            type = tokenType,
            protocol = protocol,
            underlyingTokens = listOf(token0, token1).map {
                TokenInformation(
                    network = erc20.network,
                    name = it.name,
                    symbol = it.symbol,
                    type = it.type,
                    decimals = it.decimals,
                    address = it.address,
                    totalSupply = it.totalSupply,
                )
            },
            network = erc20.network
        )
    }
}