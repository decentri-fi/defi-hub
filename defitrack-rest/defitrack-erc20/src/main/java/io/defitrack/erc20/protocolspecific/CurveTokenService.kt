package io.defitrack.erc20.protocolspecific

import io.defitrack.common.network.Network
import io.defitrack.erc20.ERC20Service
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.crv.CurveEthereumGraphProvider
import io.defitrack.protocol.crv.CurvePolygonGraphProvider
import io.defitrack.token.TokenInformation
import io.defitrack.token.TokenType
import org.springframework.stereotype.Component

@Component
class CurveTokenService(
    private val curveEthereumGraphProvider: CurveEthereumGraphProvider,
    private val curvePolygonGraphProvider: CurvePolygonGraphProvider,
    private val erC20Service: ERC20Service
) {
    suspend fun getTokenInformation(address: String, network: Network): TokenInformation {
        return when (network) {
            Network.ETHEREUM -> getEthereumCurveInfo(address)
            Network.POLYGON -> getPolygonCurveInfo(address)
            else -> throw java.lang.IllegalArgumentException("Not possible to get curve info for address $address and network $network")
        }
    }

    private suspend fun getPolygonCurveInfo(address: String): TokenInformation {
        return curvePolygonGraphProvider.getPool(address)?.let {
            val underlyingTokens = it.pool.coins.map { u ->
                erC20Service.getERC20(Network.POLYGON, u.id).toToken()
            }
            TokenInformation(
                name = it.token.name,
                address = it.id,
                symbol = underlyingTokens.joinToString("/") { it.symbol },
                decimals = it.token.decimals,
                type = TokenType.CURVE,
                underlyingTokens = underlyingTokens,
                protocol = Protocol.CURVE
            )
        } ?: throw java.lang.IllegalArgumentException("Not possible to get curve info for address $address")
    }

    private suspend fun getEthereumCurveInfo(address: String): TokenInformation {
        return curveEthereumGraphProvider.getPool(address)?.let {
            val underlyingTokens = it.pool.coins.map { u ->
                erC20Service.getERC20(Network.ETHEREUM, u.token.id).toToken()
            }
            TokenInformation(
                name = it.name,
                address = it.id,
                symbol = underlyingTokens.joinToString("/") { it.symbol },
                decimals = it.decimals,
                type = TokenType.CURVE,
                underlyingTokens = underlyingTokens,
                protocol = Protocol.CURVE,
            )
        } ?: throw java.lang.IllegalArgumentException("Not possible to get curve info for address $address")
    }
}