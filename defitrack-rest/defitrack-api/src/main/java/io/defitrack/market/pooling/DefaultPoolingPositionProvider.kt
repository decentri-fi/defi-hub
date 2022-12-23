package io.defitrack.market.pooling

import io.defitrack.evm.contract.BlockchainGatewayProvider
import io.defitrack.market.pooling.domain.PoolingPosition
import org.springframework.stereotype.Service
import java.math.BigInteger

@Service
class DefaultPoolingPositionProvider(
    private val poolingMarketProviders: List<PoolingMarketProvider>,
    private val gateway: BlockchainGatewayProvider,
) : PoolingPositionProvider() {
    override suspend fun fetchUserPoolings(address: String): List<PoolingPosition> {
        return poolingMarketProviders.flatMap { provider ->
            val markets = provider.getMarkets().filter { it.balanceFetcher != null }

            if (markets.isEmpty()) {
                return emptyList()
            }

            gateway.getGateway(provider.getNetwork()).readMultiCall(
                markets.map { market ->
                    market.balanceFetcher!!.toMulticall(address)
                }
            ).mapIndexed { index, retVal ->
                val market = markets[index]
                val balance = market.balanceFetcher!!.extractBalance(retVal)

                if (balance > BigInteger.ONE) {
                    PoolingPosition(
                        balance,
                        market,
                    )
                } else {
                    null
                }
            }.filterNotNull()
        }
    }
}