package io.defitrack.claimable

import io.defitrack.evm.contract.BlockchainGatewayProvider
import io.defitrack.market.farming.FarmingMarketProvider
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.math.BigInteger

@Component
class DefaultClaimableRewardProvider(
    private val farmingMarketProviders: List<FarmingMarketProvider>,
    private val blockchainGatewayProvider: BlockchainGatewayProvider
) {

    private val logger = LoggerFactory.getLogger(this::class.java)
    suspend fun claimables(address: String): List<Claimable> = coroutineScope {
        val markets = farmingMarketProviders.flatMap {
            it.getMarkets()
        }.filter {
            it.claimableRewardFetcher != null
        }.groupBy {
            it.network
        }

        if (markets.isEmpty()) {
            return@coroutineScope emptyList()
        }

        return@coroutineScope markets.map { entry ->
            async {
                try {
                    blockchainGatewayProvider.getGateway(entry.key).readMultiCall(
                        entry.value.map { market ->
                            market.claimableRewardFetcher!!.toMulticall(address)
                        }
                    ).mapIndexed { index, retVal ->
                        val market = entry.value[index]
                        val earned = market.claimableRewardFetcher!!.extract(retVal)

                        if (earned > BigInteger.ONE) {
                            Claimable(
                                id = "rwrd_${market.id}",
                                type = market.contractType,
                                name = market.name,
                                protocol = market.protocol,
                                network = market.network,
                                amount = earned,
                                claimableTokens = market.rewardTokens,
                                claimTransaction = market.claimableRewardFetcher.preparedTransaction(address),
                            )
                        } else {
                            null
                        }
                    }.filterNotNull()
                } catch (ex: Exception) {
                    logger.info("Unable to fetch claimables for ${entry.key}", ex)
                    emptyList()
                }
            }
        }.awaitAll().flatten()
    }
}