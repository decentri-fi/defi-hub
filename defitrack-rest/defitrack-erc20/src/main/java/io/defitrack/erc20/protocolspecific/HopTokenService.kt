package io.defitrack.erc20.protocolspecific

import io.defitrack.abi.ABIResource
import io.defitrack.common.network.Network
import io.defitrack.erc20.ERC20Service
import io.defitrack.evm.contract.BlockchainGatewayProvider
import io.defitrack.protocol.HopService
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.contract.HopLpTokenContract
import io.defitrack.token.TokenInformation
import io.defitrack.token.TokenType
import org.springframework.stereotype.Component

@Component
class HopTokenService(
    private val abiResource: ABIResource,
    private val blockchainGatewayProvider: BlockchainGatewayProvider,
    private val hopService: HopService,
    private val erC20Service: ERC20Service
) {

    suspend fun getTokenInformation(
        address: String,
        network: Network
    ): TokenInformation {
        return hopService.getLps(network).find {
            it.lpToken.lowercase() == address.lowercase()
        }!!.let { hopLpToken ->

            val saddleToken = HopLpTokenContract(
                blockchainGateway = blockchainGatewayProvider.getGateway(network),
                abiResource.getABI("hop/SaddleToken.json"),
                address
            )

            val token0 = erC20Service.getERC20(network, hopLpToken.canonicalToken)
            val token1 = erC20Service.getERC20(network, hopLpToken.hToken)

            TokenInformation(
                name = saddleToken.name(),
                symbol = saddleToken.symbol(),
                address = address,
                decimals = saddleToken.decimals(),
                totalSupply = saddleToken.totalSupply(),
                type = TokenType.HOP,
                protocol = Protocol.HOP,
                underlyingTokens = listOf(
                    token0.toToken(), token1.toToken(),
                )
            )
        }
    }
}