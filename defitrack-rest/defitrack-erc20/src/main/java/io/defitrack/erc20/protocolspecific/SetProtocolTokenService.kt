package io.defitrack.erc20.protocolspecific

import io.defitrack.common.network.Network
import io.defitrack.evm.contract.BlockchainGatewayProvider
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.set.EthereumSetProvider
import io.defitrack.protocol.set.PolygonAbstractSetProvider
import io.defitrack.protocol.set.SetTokenContract
import io.defitrack.token.ERC20Resource
import io.defitrack.token.TokenInformation
import io.defitrack.token.TokenType
import org.springframework.stereotype.Component

@Component
class SetProtocolTokenService(
    private val ethereumSetProvider: EthereumSetProvider,
    private val polygonSetProvider: PolygonAbstractSetProvider,
    private val blockchainGatewayProvider: BlockchainGatewayProvider,
    private val erC20Resource: ERC20Resource
) {

    val providers = mapOf(
        Network.ETHEREUM to ethereumSetProvider,
        Network.POLYGON to polygonSetProvider
    )

    suspend fun isSetToken(
        address: String,
        network: Network
    ): Boolean {
        return providers[network]?.let {
            it.getSets().map { it.lowercase() }.contains(address.lowercase())
        } ?: false
    }

    suspend fun getTokenInformation(address: String, network: Network): TokenInformation {
        val contract = SetTokenContract(
            blockchainGatewayProvider.getGateway(network),
            address
        )

        return TokenInformation(
            network = network,
            name = contract.name(),
            symbol = contract.symbol(),
            type = TokenType.SET,
            decimals = contract.decimals(),
            address = address,
            protocol = Protocol.SET,
            totalSupply = contract.totalSupply(),
            underlyingTokens = contract.getPositions().map {
                val info = erC20Resource.getTokenInformation(network, it.token)
                TokenInformation(
                    network = network,
                    name = info.name,
                    symbol = info.symbol,
                    type = TokenType.SINGLE,
                    decimals = info.decimals,
                    address = it.token,
                    totalSupply = info.totalSupply,
                )
            }
        )
    }
}