package io.defitrack.erc20.protocolspecific

import io.defitrack.abi.ABIResource
import io.defitrack.common.utils.Refreshable
import io.defitrack.erc20.ERC20
import io.defitrack.erc20.ERC20ContractReader
import io.defitrack.erc20.ERC20ToTokenInformationMapper
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
    private val erC20ContractReader: ERC20ContractReader,
    private val erC20ToTokenInformationMapper: ERC20ToTokenInformationMapper
) : TokenIdentifier {

    override suspend fun getTokenInfo(
        token: ERC20,
    ): TokenInformation {
        return hopService.getLps(token.network).find {
            it.lpToken.lowercase() == token.address.lowercase()
        }!!.let { hopLpToken ->

            val saddleToken = HopLpTokenContract(
                blockchainGateway = blockchainGatewayProvider.getGateway(token.network),
                abiResource.getABI("hop/SaddleToken.json"),
                token.address
            )

            val token0 = erC20ContractReader.getERC20(token.network, hopLpToken.canonicalToken)
            val token1 = erC20ContractReader.getERC20(token.network, hopLpToken.hToken)

            TokenInformation(
                name = saddleToken.name(),
                symbol = saddleToken.symbol(),
                address = token.address,
                decimals = saddleToken.decimals(),
                totalSupply = Refreshable.refreshable {
                    saddleToken.totalSupply()
                },
                type = TokenType.HOP,
                protocol = Protocol.HOP,
                underlyingTokens = listOf(
                    token0, token1,
                ).map {
                    erC20ToTokenInformationMapper.map(it, TokenType.HOP, Protocol.HOP)
                },
                network = token.network
            )
        }
    }

    override suspend fun isProtocolToken(token: ERC20): Boolean {
        return token.symbol.startsWith("HOP-LP")
    }
}