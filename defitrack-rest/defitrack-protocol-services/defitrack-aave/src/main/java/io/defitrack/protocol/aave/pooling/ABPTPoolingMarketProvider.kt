package io.defitrack.protocol.aave.pooling

import io.defitrack.common.network.Network
import io.defitrack.erc20.TokenInformationVO
import io.defitrack.market.pooling.PoolingMarketProvider
import io.defitrack.market.pooling.domain.PoolingMarket
import io.defitrack.protocol.Protocol
import io.defitrack.token.TokenType
import org.springframework.stereotype.Component

@Component
class ABPTPoolingMarketProvider() : PoolingMarketProvider(
) {

    val abptAddress = "0x41a08648c3766f9f9d85598ff102a08f4ef84f84"
    val aaveAddress = "0x7fc66500c84a76ad7e9c93437bfc5ac33e2ddae9"
    val wethAddress = "0xc02aaa39b223fe8d0a0e5c4f27ead9083c756cc2"


    override suspend fun fetchMarkets(): List<PoolingMarket> {
        val token = getToken(abptAddress)
        val aave = getToken(aaveAddress)
        val weth = getToken(wethAddress)

        return listOf(
            PoolingMarket(
                id = "pool_aave-ethereum-abpt",
                network = getNetwork(),
                protocol = getProtocol(),
                address = abptAddress,
                name = "Aave Balance Pool Token",
                symbol = "ABPT",
                tokens = listOf(
                    aave, weth
                ).map(TokenInformationVO::toFungibleToken),
                tokenType = TokenType.BALANCER,
                totalSupply = token.totalSupply
            )
        )
    }

    override fun getNetwork(): Network {
        return Network.ETHEREUM
    }
}