package io.defitrack.erc20.application.protocolspecific

import io.defitrack.erc20.ERC20
import io.defitrack.evm.contract.BlockchainGatewayProvider
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.balancer.contract.BalancerPoolContract
import io.defitrack.protocol.balancer.contract.BalancerService
import io.defitrack.protocol.balancer.contract.BalancerVaultContract
import io.defitrack.erc20.domain.TokenInformation
import io.defitrack.token.TokenType
import org.springframework.stereotype.Component

@Component
class BalancerLPIdentifier(
    private val balancerService: BalancerService,
    private val blockchainGatewayProvider: BlockchainGatewayProvider
) : TokenIdentifier() {

    override suspend fun isProtocolToken(token: ERC20): Boolean {
        return balancerService.getPools(token.network).any {
            it.lowercase() == token.address.lowercase()
        }
    }

    override suspend fun getTokenInfo(token: ERC20): TokenInformation {
        val gateway = blockchainGatewayProvider.getGateway(token.network)
        val poolContract = BalancerPoolContract(
            gateway,
            token.address
        )

        val vault = BalancerVaultContract(
            gateway,
            poolContract.vault.await()
        )

        val poolId = poolContract.getPoolId()

        val poolTokens = vault.getPoolTokens(poolId, token.address)
        val underlying = poolTokens
            .filter {
                it.token.lowercase() != token.address.lowercase()
            }.map {
                erc20TokenService.getTokenInformation(it.token, token.network)
            }.filter {
                it.isSome()
            }.map {
                it.getOrNull()!!
            }

        return TokenInformation(
            network = token.network,
            name = underlying.joinToString("/") {
                it.symbol
            },
            protocol = Protocol.BALANCER,
            address = token.address,
            underlyingTokens = underlying,
            decimals = token.decimals,
            type = TokenType.CUSTOM_LP,
            totalSupply = token.totalSupply,
            symbol = underlying.joinToString("/") {
                it.symbol
            },
        )
    }
}