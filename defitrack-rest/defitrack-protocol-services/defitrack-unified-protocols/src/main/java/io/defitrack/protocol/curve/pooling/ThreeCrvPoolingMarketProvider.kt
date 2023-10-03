package io.defitrack.protocol.curve.pooling

import io.defitrack.common.network.Network
import io.defitrack.common.utils.FormatUtilsExtensions.asEth
import io.defitrack.common.utils.Refreshable.Companion.refreshable
import io.defitrack.conditional.ConditionalOnCompany
import io.defitrack.market.pooling.PoolingMarketProvider
import io.defitrack.market.pooling.domain.PoolingMarket
import io.defitrack.protocol.Company
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.crv.contract.CurvePoolContract
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component

@Component
@ConditionalOnCompany(Company.CURVE)
@ConditionalOnProperty(value = ["ethereum.enabled"], havingValue = "true", matchIfMissing = true)
class ThreeCrvPoolingMarketProvider : PoolingMarketProvider() {

    val address = "0x6c3F90f043a72FA612cbac8115EE7e52BDe6E490"
    val usdt = "0xdac17f958d2ee523a2206206994597c13d831ec7"
    val usdc = "0xa0b86991c6218b36c1d19d4a2e9eb0ce3606eb48"
    val dai = "0x6b175474e89094c44da98b954eedeac495271d0f"

    val pool = "0xbebc44782c7db0a1a60cb6fe97d0b483032ff1c7"

    override suspend fun produceMarkets(): Flow<PoolingMarket> = channelFlow {

        val token = getToken(address)
        val poolContract = CurvePoolContract(getBlockchainGateway(), pool)

        val underlyingTokens = listOf(
            getToken(usdt),
            getToken(usdc),
            getToken(dai)
        ).map { it.toFungibleToken() }

        send(
            create(
                name = "3CRV",
                identifier = "3crv",
                address = address,
                symbol = "3CRV",
                positionFetcher = defaultPositionFetcher(address),
                price = refreshable {
                    poolContract.virtualPrice.await().asEth(token.decimals)
                },
                marketSize = refreshable {
                    getMarketSize(
                        underlyingTokens, pool
                    )
                },
                tokens = underlyingTokens,
                erc20Compatible = true,
                totalSupply = refreshable(token.totalDecimalSupply()) {
                    getToken(address).totalDecimalSupply()
                },
            )
        )
    }

    override fun getProtocol(): Protocol {
        return Protocol.CURVE
    }

    override fun getNetwork(): Network {
        return Network.ETHEREUM
    }
}