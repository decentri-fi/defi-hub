package io.defitrack.market.lending

import io.defitrack.evm.contract.BlockchainGatewayProvider
import io.defitrack.market.lending.domain.LendingPosition
import org.springframework.stereotype.Service
import java.math.BigInteger

@Service
class DefaultLendingPositionProvider(
    val lendingMarkets: List<LendingMarketProvider>,
    val gateway: BlockchainGatewayProvider
) : LendingPositionProvider() {
    override suspend fun getLendings(address: String): List<LendingPosition> {
        return lendingMarkets.flatMap { provider ->
            val markets = provider.getMarkets().filter { it.positionFetcher != null }
            if (markets.isEmpty()) {
                return@flatMap emptyList()
            }

            gateway.getGateway(provider.getNetwork()).readMultiCall(
                markets.map { market ->
                    market.positionFetcher!!.toMulticall(address)
                }
            ).mapIndexed { index, retVal ->
                val market = markets[index]
                val balance = market.positionFetcher!!.extractBalance(retVal.data)

                if (balance.underlyingAmount > BigInteger.ONE) {
                    LendingPosition(
                        balance.underlyingAmount,
                        balance.tokenAmount,
                        market,
                    )
                } else {
                    null
                }
            }.filterNotNull()
        }
    }
}