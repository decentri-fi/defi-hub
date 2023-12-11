package io.defitrack.erc20.protocolspecific

import arrow.core.getOrElse
import io.defitrack.common.network.Network
import io.defitrack.erc20.ERC20
import io.defitrack.erc20.ERC20ContractReader
import io.defitrack.evm.contract.BlockchainGatewayProvider
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.set.EthereumSetProvider
import io.defitrack.protocol.set.PolygonSetProvider
import io.defitrack.protocol.set.SetTokenContract
import io.defitrack.erc20.TokenInformation
import io.defitrack.token.TokenType
import org.springframework.stereotype.Component

@Component
class SetProtocolTokenService(
    ethereumSetProvider: EthereumSetProvider,
    polygonSetProvider: PolygonSetProvider,
    private val blockchainGatewayProvider: BlockchainGatewayProvider,
    private val contractReader: ERC20ContractReader
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

        val asERC20 = contractReader.getERC20(token.network, token.address).getOrElse {
            throw IllegalArgumentException("couldn't get erc20 token for ${token.address} on ${token.network.name}")
        }

        return TokenInformation(
            network = token.network,
            name = asERC20.name,
            symbol = asERC20.symbol,
            type = TokenType.CUSTOM_LP,
            decimals = contract.readDecimals().toInt(),
            address = token.address,
            protocol = Protocol.SET,
            totalSupply = contract.totalSupply(),
            underlyingTokens = contract.getPositions().mapNotNull {
                erc20Service.getTokenInformation(it.token, token.network).getOrNull()
            }
        )
    }
}