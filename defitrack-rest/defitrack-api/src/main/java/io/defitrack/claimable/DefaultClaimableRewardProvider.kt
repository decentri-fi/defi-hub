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
                    val rewards = entry.value.flatMap { market ->
                        market.claimableRewardFetcher!!.rewards.map {
                            it to market
                        }
                    }

                    blockchainGatewayProvider.getGateway(entry.key).readMultiCall(
                        rewards.map { it.first.toMulticall(address) }
                    ).mapIndexed { index, retVal ->
                        val reward = rewards[index]
                        val earned = reward.first.extractAmountFromRewardFunction(retVal.data)

                        if (earned > BigInteger.ONE) {

                            Claimable(
                                id = "rwrd_${reward.second.id}",
                                name = reward.second.name,
                                protocol = reward.second.protocol,
                                network = reward.second.network,
                                amount = earned,
                                claimableToken = reward.first.token,
                                claimTransaction = reward.second.claimableRewardFetcher!!.preparedTransaction.invoke(address),
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