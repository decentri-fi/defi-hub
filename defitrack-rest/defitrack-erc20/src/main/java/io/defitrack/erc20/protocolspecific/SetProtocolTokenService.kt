package io.defitrack.erc20.protocolspecific

import io.defitrack.common.network.Network
import io.defitrack.erc20.ERC20
import io.defitrack.evm.contract.BlockchainGatewayProvider
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.set.EthereumSetProvider
import io.defitrack.protocol.set.PolygonSetProvider
import io.defitrack.protocol.set.SetTokenContract
import io.defitrack.token.TokenInformation
import io.defitrack.token.TokenType
import org.springframework.stereotype.Component

@Component
class SetProtocolTokenService(
    ethereumSetProvider: EthereumSetProvider,
    polygonSetProvider: PolygonSetProvider,
    private val blockchainGatewayProvider: BlockchainGatewayProvider,
) : TokenIdentifier() {

    val providers = mapOf(
        Network.ETHEREUM to ethereumSetProvider,
        Network.POLYGON to polygonSetProvider
    )

    override suspend fun isProtocolToken(
        token: ERC20,
    ): Boolean {
        return providers[token.network]?.getSets()?.map(String::lowercase)?.contains(token.address.lowercase()) ?: false
    }

    override suspend fun getTokenInfo(token: ERC20): TokenInformation {
        val contract = SetTokenContract(
            blockchainGatewayProvider.getGateway(token.network),
            token.address
        )

        return TokenInformation(
            network = token.network,
            name = contract.readName(),
            symbol = contract.readSymbol(),
            type = TokenType.CUSTOM_LP,
            decimals = contract.readDecimals().toInt(),
            address = token.address,
            protocol = Protocol.SET,
            totalSupply = contract.totalSupply(),
            underlyingTokens = contract.getPositions().mapNotNull {
                erc20Service.getTokenInformation(token.address, token.network).getOrNull()
            }
        )
    }
}