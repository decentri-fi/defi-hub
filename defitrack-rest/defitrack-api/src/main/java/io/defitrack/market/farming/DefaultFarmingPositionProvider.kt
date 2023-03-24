package io.defitrack.market.farming

import io.defitrack.evm.contract.BlockchainGatewayProvider
import io.defitrack.market.farming.domain.FarmingPosition
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import org.springframework.stereotype.Service
import java.math.BigInteger

@Service
class DefaultFarmingPositionProvider(
    val farmingMarketProvider: List<FarmingMarketProvider>,
    val gateway: BlockchainGatewayProvider
) : FarmingPositionProvider() {
    override suspend fun getStakings(address: String): List<FarmingPosition> = coroutineScope {

        val semaphore = Semaphore(16)

        farmingMarketProvider.flatMap { provider ->
            val markets = provider.getMarkets().filter { it.balanceFetcher != null }
            if (markets.isEmpty()) {
                return@flatMap emptyList()
            }

            gateway.getGateway(provider.getNetwork()).readMultiCall(
                markets.map { market ->
                    market.balanceFetcher!!.toMulticall(address)
                }
            ).mapIndexed { index, retVal ->
                semaphore.withPermit {
                    async {
                        val market = markets[index]
                        val balance = market.balanceFetcher!!.extractBalance(retVal)

                        if (balance.underlyingAmount > BigInteger.ONE) {
                            FarmingPosition(
                                market,
                                balance.underlyingAmount,
                                balance.tokenAmount
                            )
                        } else {
                            null
                        }
                    }
                }
            }.awaitAll().filterNotNull()
        }
    }
}