package io.defitrack.erc20.protocolspecific

import io.defitrack.erc20.ERC20
import io.defitrack.erc20.LpContractReader
import io.defitrack.evm.contract.BlockchainGatewayProvider
import io.defitrack.protocol.Protocol
import io.defitrack.erc20.TokenInformation
import io.defitrack.token.TokenType
import org.springframework.beans.factory.annotation.Autowired

abstract class DefaultLpIdentifier(
    private val protocol: Protocol,
) : TokenIdentifier() {

    @Autowired
    protected lateinit var lpContractReader: LpContractReader
    @Autowired
    protected lateinit var blockchainGatewayProvider: BlockchainGatewayProvider

    override suspend fun getTokenInfo(token: ERC20): TokenInformation {
        return fromLP(protocol, token, TokenType.STANDARD_LP)
    }

    suspend fun fromLP(protocol: Protocol, erc20: ERC20, tokenType: TokenType): TokenInformation {
        val lp = lpContractReader.getLP(erc20.network, erc20.address)

        val token0 = erc20Service.getTokenInformation(lp.token0.await(), erc20.network)
        val token1 = erc20Service.getTokenInformation(lp.token1.await(), erc20.network)

        if (token0.isNone() || token1.isNone()) {
            throw IllegalStateException("Token0 or Token1 is not an erc20 for ${erc20.address}")
        }

        return TokenInformation(
            name = "${token0.getOrNull()!!.symbol}/${token1.getOrNull()!!.symbol} LP",
            symbol = "${token0.getOrNull()!!.symbol}-${token1.getOrNull()!!.symbol}",
            address = erc20.address,
            decimals = erc20.decimals,
            totalSupply = lp.totalSupply(),
            type = tokenType,
            protocol = protocol,
            underlyingTokens = listOf(token0.getOrNull()!!, token1.getOrNull()!!),
            network = erc20.network
        )
    }
}