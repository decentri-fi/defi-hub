package io.defitrack.erc20.protocolspecific

import io.defitrack.common.network.Network
import io.defitrack.erc20.ERC20Service
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.balancer.polygon.BalancerPolygonPoolGraphProvider
import io.defitrack.token.TokenInformation
import io.defitrack.token.TokenType
import org.springframework.stereotype.Component

@Component
class BalancerTokenService(
    balancerPolygonService: BalancerPolygonPoolGraphProvider,
    private val erC20Service: ERC20Service
) {

    val balancerServices = mapOf(
        Network.POLYGON to balancerPolygonService
    )

    suspend fun isBalancerToken(
        address: String,
        network: Network
    ): Boolean {
        return getBalancerService(network)?.let {
            it.getPool(address) != null
        } ?: false
    }

    fun getBalancerService(network: Network): BalancerPolygonPoolGraphProvider? {
        return balancerServices[network]
    }

    suspend fun getTokenInformation(address: String, network: Network): TokenInformation {
        return getBalancerService(network)?.getPool(address)?.let {
            val erc20 = erC20Service.getERC20(network, address)
            TokenInformation(
                name = it.name,
                symbol = it.symbol,
                address = it.address,
                decimals = erc20.decimals,
                type = TokenType.BALANCER,
                protocol = Protocol.BALANCER,
                underlyingTokens = it.tokens.map { poolToken ->
                    TokenInformation(
                        name = poolToken.name,
                        symbol = poolToken.symbol,
                        address = poolToken.address,
                        decimals = poolToken.decimals,
                        type = TokenType.SINGLE
                    )
                }
            )
        } ?: throw java.lang.IllegalArgumentException("Pool with $address not found as balancer pool")
    }
}