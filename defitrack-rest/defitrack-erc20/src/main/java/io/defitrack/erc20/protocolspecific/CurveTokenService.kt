package io.defitrack.erc20.protocolspecific

import io.defitrack.erc20.ERC20
import io.defitrack.erc20.ERC20ContractReader
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.crv.CurvePoolGraphProvider
import io.defitrack.token.TokenInformation
import io.defitrack.token.TokenType
import org.springframework.stereotype.Component

@Component
class CurveTokenService(
    private val curvePoolGraphProviders: List<CurvePoolGraphProvider>,
    private val erC20ContractReader: ERC20ContractReader
) : TokenIdentifier {
    override suspend fun getTokenInfo(token: ERC20): TokenInformation {
        return getCurveInfo(token) ?: getDefaultInfo(token)
    }

    private suspend fun getDefaultInfo(erC20: ERC20): TokenInformation {
        return erC20.toToken().copy(
            type = TokenType.CURVE,
            protocol = Protocol.CURVE
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
                    erC20ContractReader.getERC20(token.network, coin).toToken()
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
                    totalSupply = token.totalSupply
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
