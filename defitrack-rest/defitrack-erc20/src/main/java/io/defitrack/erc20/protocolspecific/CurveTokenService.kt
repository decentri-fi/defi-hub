package io.defitrack.erc20.protocolspecific

import io.defitrack.common.utils.Refreshable
import io.defitrack.erc20.ERC20
import io.defitrack.erc20.ERC20ContractReader
import io.defitrack.erc20.ERC20ToTokenInformationMapper
import io.defitrack.erc20.TokenService
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.crv.CurvePoolGraphProvider
import io.defitrack.token.TokenInformation
import io.defitrack.token.TokenType
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class CurveTokenService(
    private val curvePoolGraphProviders: List<CurvePoolGraphProvider>,
    private val erC20ContractReader: ERC20ContractReader,
    private val erc20ToTokenInformationMapper: ERC20ToTokenInformationMapper
) : TokenIdentifier {


    @Autowired
    private lateinit var tokenService: TokenService

    override suspend fun getTokenInfo(token: ERC20): TokenInformation {
        return getCurveInfo(token) ?: erc20ToTokenInformationMapper.map(
            token, TokenType.CURVE, Protocol.CURVE
        )
    }

    private suspend fun getCurveInfo(token: ERC20): TokenInformation? {
        val provider = curvePoolGraphProviders.find {
            it.network == token.network
        }

        val pool = provider?.getPoolByLp(token.address)
        return if (pool != null) {
            try {
                val underlyingTokens = pool.coins.map { coin ->
                    tokenService.getTokenInformation(coin, token.network)
                }

                TokenInformation(
                    name = token.name,
                    address = token.address,
                    symbol = underlyingTokens.joinToString("/") { it.symbol },
                    decimals = token.decimals,
                    type = TokenType.CURVE,
                    underlyingTokens = underlyingTokens,
                    protocol = Protocol.CURVE,
                    network = token.network,
                    totalSupply = Refreshable.refreshable(token.totalSupply) {
                        erC20ContractReader.getERC20(token.network, token.address).totalSupply
                    }
                )
            } catch (ex: Exception) {
                null
            }
        } else {
            null
        }
    }

    override suspend fun isProtocolToken(token: ERC20): Boolean {
        return token.name.lowercase().startsWith("curve.fi".lowercase())
    }
}
