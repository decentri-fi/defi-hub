package io.defitrack.erc20.protocolspecific

import io.defitrack.common.network.Network
import io.defitrack.erc20.ERC20Service
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.crv.CurvePoolGraphProvider
import io.defitrack.token.TokenInformation
import io.defitrack.token.TokenType
import org.springframework.stereotype.Component

@Component
class CurveTokenService(
    private val curvePoolGraphProviders: List<CurvePoolGraphProvider>,
    private val erC20Service: ERC20Service
) {
    suspend fun getTokenInformation(address: String, network: Network): TokenInformation {
        return getCurveInfo(address, network) ?: getDefaultInfo(address, network)
    }

    private suspend fun getDefaultInfo(address: String, network: Network): TokenInformation {
        return erC20Service.getERC20(network, address).toToken().copy(
            type = TokenType.CURVE,
            protocol = Protocol.CURVE
        )
    }

    private suspend fun getCurveInfo(address: String, network: Network): TokenInformation? {
        val provider = curvePoolGraphProviders.find {
            it.network == network
        }

        val pool = provider?.getPoolByLp(address)
        return if (pool != null) {
            try {
                val underlyingTokens = pool.coins.map { coin ->
                    erC20Service.getERC20(network, coin).toToken()
                }

                val token = erC20Service.getERC20(network, address)

                TokenInformation(
                    name = token.name,
                    address = address,
                    symbol = underlyingTokens.joinToString("/") { it.symbol },
                    decimals = token.decimals,
                    type = TokenType.CURVE,
                    underlyingTokens = underlyingTokens,
                    protocol = Protocol.CURVE,
                )
            } catch (ex: Exception) {
                null
            }
        } else {
            null
        }
    }
}
