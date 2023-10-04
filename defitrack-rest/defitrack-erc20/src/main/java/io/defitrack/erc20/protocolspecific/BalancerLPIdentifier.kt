package io.defitrack.erc20.protocolspecific

import io.defitrack.common.utils.Refreshable
import io.defitrack.erc20.ERC20
import io.defitrack.erc20.ERC20Service
import io.defitrack.evm.contract.BlockchainGatewayProvider
import io.defitrack.protocol.balancer.contract.BalancerPoolContract
import io.defitrack.protocol.balancer.contract.BalancerService
import io.defitrack.protocol.balancer.contract.BalancerVaultContract
import io.defitrack.token.TokenInformation
import io.defitrack.token.TokenType
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class BalancerLPIdentifier(
    private val balancerService: BalancerService,
    private val blockchainGatewayProvider: BlockchainGatewayProvider
) : TokenIdentifier {

    @Autowired
    private lateinit var erc20Service: ERC20Service


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
            poolContract.getVault()
        )

        val poolId = poolContract.getPoolId()

        val poolTokens = vault.getPoolTokens(poolId)
        val underlying = poolTokens.tokens
            .filter {
                it.lowercase() != token.address.lowercase()
            }
            .map { u ->
                erc20Service.getTokenInformation(u, token.network)
            }

        return TokenInformation(
            network = token.network,
            name = underlying.joinToString("/") {
                it.symbol
            },
            address = token.address,
            underlyingTokens = underlying,
            decimals = token.decimals,
            type = TokenType.BALANCER,
            totalSupply = Refreshable.refreshable { token.totalSupply },
            symbol = underlying.joinToString("/") {
                it.symbol
            },
        )
    }
}