package io.defitrack.market.farming

import io.defitrack.evm.contract.BlockchainGatewayProvider
import io.defitrack.market.farming.domain.FarmingPosition
import io.defitrack.token.ERC20Resource
import org.springframework.stereotype.Service
import java.math.BigInteger

@Service
class DefaultFarmingPositionProvider(
    val farmingMarketProvider: List<FarmingMarketProvider>,
    val gateway: BlockchainGatewayProvider
) : FarmingPositionProvider() {
    override suspend fun getStakings(address: String): List<FarmingPosition> {
        return farmingMarketProvider.flatMap { provider ->
            val markets = provider.getMarkets().filter { it.balanceFetcher != null }
            if (markets.isEmpty()) {
                return@flatMap emptyList()
            }

            gateway.getGateway(provider.getNetwork()).readMultiCall(
                markets.map { market ->
                    market.balanceFetcher!!.toMulticall(address)
                }
            ).mapIndexed { index, retVal ->
                val market = markets[index]
                val balance = market.balanceFetcher!!.extractBalance(retVal)

                if (balance > BigInteger.ONE) {
                    FarmingPosition(
                        market,
                        balance,
                    )
                } else {
                    null
                }
            }.filterNotNull()
        }
    }
}