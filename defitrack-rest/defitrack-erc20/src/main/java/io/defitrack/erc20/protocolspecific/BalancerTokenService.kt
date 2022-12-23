package io.defitrack.erc20.protocolspecific

import io.defitrack.common.network.Network
import io.defitrack.erc20.ERC20Service
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.balancer.BalancerPoolGraphProvider
import io.defitrack.protocol.balancer.polygon.BalancerPolygonPoolGraphProvider
import io.defitrack.protocol.graph.BeethovenXFantomGraphProvider
import io.defitrack.protocol.graph.BeethovenXOptimismGraphProvider
import io.defitrack.token.TokenInformation
import io.defitrack.token.TokenType
import org.springframework.stereotype.Component

@Component
class BalancerTokenService(
    private val poolGraphProviders: List<BalancerPoolGraphProvider>,
    private val erC20Service: ERC20Service
) {

    suspend fun isBalancerToken(
        address: String,
        network: Network
    ): Boolean {
        return getBalancerService(network)?.let {
            it.getPool(address) != null
        } ?: false
    }

    fun getBalancerService(network: Network): BalancerPoolGraphProvider? {
        return this.poolGraphProviders.find {
            it.getNetwork() == network
        }
    }

    suspend fun getTokenInformation(address: String, network: Network): TokenInformation {
        val balancerService = getBalancerService(network)
        return balancerService?.getPool(address)?.let {
            val erc20 = erC20Service.getERC20(network, address)
            TokenInformation(
                name = it.name,
                symbol = it.symbol,
                address = it.address,
                decimals = erc20.decimals,
                type = TokenType.BALANCER,
                protocol = balancerService.getProtocol(),
                underlyingTokens = it.tokens.map { poolToken ->
                    TokenInformation(
                        name = poolToken.name,
                        symbol = poolToken.symbol,
                        address = poolToken.address,
                        decimals = poolToken.decimals,
                        type = TokenType.SINGLE,
                        network = network
                    )
                },
                network = network
            )
        } ?: throw java.lang.IllegalArgumentException("Pool with $address not found as balancer pool")
    }
}