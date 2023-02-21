package io.defitrack.erc20.protocolspecific

import io.defitrack.erc20.ERC20
import io.defitrack.erc20.LpContractReader
import io.defitrack.protocol.Protocol
import io.defitrack.token.ERC20Resource
import io.defitrack.token.TokenInformation
import io.defitrack.token.TokenType

abstract class DefaultLpIdentifier(
    private val protocol: Protocol,
    private val tokenType: TokenType,
    private val erC20Resource: ERC20Resource,
    private val lpContractReader: LpContractReader
) : TokenIdentifier {

    override suspend fun getTokenInfo(token: ERC20): TokenInformation {
        return fromLP(protocol, token, tokenType)
    }

    suspend fun fromLP(protocol: Protocol, erc20: ERC20, tokenType: TokenType): TokenInformation {
        val lp = lpContractReader.getLP(erc20.network, erc20.address)

        val token0 = erC20Resource.getTokenInformation(erc20.network, lp.token0())
        val token1 = erC20Resource.getTokenInformation(erc20.network, lp.token1())

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