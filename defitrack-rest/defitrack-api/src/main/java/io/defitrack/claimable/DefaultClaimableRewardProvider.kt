package io.defitrack.claimable

import io.defitrack.evm.contract.BlockchainGatewayProvider
import io.defitrack.market.farming.ClaimableMarketProvider
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.math.BigInteger

@Component
class DefaultClaimableRewardProvider(
    private val farmingMarketProviders: List<ClaimableMarketProvider>,
    private val blockchainGatewayProvider: BlockchainGatewayProvider
) {

    private val logger = LoggerFactory.getLogger(this::class.java)
    suspend fun claimables(address: String): List<UserClaimable> = coroutineScope {
        val fetchersByNetwork = farmingMarketProviders.flatMap {
            it.getClaimables()
        }.groupBy {
            it.network
        }

        if (fetchersByNetwork.isEmpty()) {
            return@coroutineScope emptyList()
        }

        return@coroutineScope fetchersByNetwork.map { entry ->
            async {
                try {
                    val rewards = entry.value.flatMap { claimable ->
                        claimable.claimableRewardFetcher.rewards.map {
                            it to claimable
                        }
                    }

                    blockchainGatewayProvider.getGateway(entry.key).readMultiCall(
                        rewards.map { it.first.toMulticall(address) }
                    ).mapIndexed { index, retVal ->

                        if (retVal.success) {


                            val reward = rewards[index]
                            val earned = reward.first.extractAmountFromRewardFunction(retVal.data)

                            if (earned > BigInteger.ONE) {

                                UserClaimable(
                                    id = "rwrd_${reward.second.id}",
                                    name = reward.second.name,
                                    protocol = reward.second.protocol,
                                    network = reward.second.network,
                                    amount = earned,
                                    claimableToken = reward.first.token,
                                    claimTransaction = reward.second.claimableRewardFetcher.preparedTransaction.invoke(
                                        address
                                    ),
                                )
                            } else {
                                null
                            }
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