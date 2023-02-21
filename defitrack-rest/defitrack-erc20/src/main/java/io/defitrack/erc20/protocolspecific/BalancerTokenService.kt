package io.defitrack.erc20.protocolspecific

import io.defitrack.common.network.Network
import io.defitrack.erc20.ERC20
import io.defitrack.erc20.ERC20ContractReader
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.balancer.BalancerPoolGraphProvider
import io.defitrack.token.ERC20Resource
import io.defitrack.token.TokenInformation
import io.defitrack.token.TokenType
import org.springframework.stereotype.Component

@Component
class BalancerTokenService(
    private val poolGraphProviders: List<BalancerPoolGraphProvider>,
    private val erC20ContractReader: ERC20ContractReader,
    private val erC20Resource: ERC20Resource
) : TokenIdentifier {

    override suspend fun isProtocolToken(
        token: ERC20,
    ): Boolean {
        return getBalancerService(token.network)?.let {
            it.getPool(token.address) != null
        } ?: false
    }

    fun getBalancerService(network: Network): BalancerPoolGraphProvider? {
        return this.poolGraphProviders.find {
            it.getNetwork() == network
        }
    }

    override suspend fun getTokenInfo(token: ERC20): TokenInformation {
        val balancerService = getBalancerService(token.network)
        return balancerService?.getPool(token.address)?.let {
            val erc20 = erC20ContractReader.getERC20(token.network, token.address)
            val underlying = it.tokens.filter { underlying ->
                underlying.address != token.address //bug in balancer graph
            }


            TokenInformation(
                name = it.name,
                symbol = it.symbol,
                address = it.address,
                decimals = erc20.decimals,
                type = TokenType.BALANCER,
                protocol = Protocol.BALANCER,
                underlyingTokens = underlying.map { underlyingPoolToken ->
                    val underlying = erC20Resource.getTokenInformation(erc20.network, underlyingPoolToken.address)
                    TokenInformation(
                        name = underlyingPoolToken.name,
                        symbol = underlyingPoolToken.symbol,
                        address = underlyingPoolToken.address,
                        decimals = underlyingPoolToken.decimals,
                        type = underlying.type,
                        network = token.network
                    )
                },
                network = token.network
            )
        } ?: throw java.lang.IllegalArgumentException("Pool with ${token.address} not found as balancer pool")
    }
}