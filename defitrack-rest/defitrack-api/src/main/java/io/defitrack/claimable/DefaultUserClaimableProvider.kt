package io.defitrack.claimable

import arrow.core.*
import arrow.core.Either.Companion.catch
import io.defitrack.claimable.domain.ClaimableMarket
import io.defitrack.claimable.domain.ClaimableRewardFetcher
import io.defitrack.claimable.domain.Reward
import io.defitrack.claimable.domain.UserClaimable
import io.defitrack.evm.contract.BlockchainGatewayProvider
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.math.BigInteger
import kotlin.collections.flatten

@Component
class DefaultUserClaimableProvider(
    private val claimableMarketProviders: List<AbstractClaimableMarketProvider>,
    private val blockchainGatewayProvider: BlockchainGatewayProvider
) {

    private val logger = LoggerFactory.getLogger(this::class.java)
    suspend fun claimables(userAddress: String, protocol: String?): List<UserClaimable> = coroutineScope {
        val fetchersByNetwork = claimableMarketProviders.flatMap { it.getMarkets() }
            .filter {
                protocol == null || it.protocol.slug.lowercase() == protocol.lowercase() ||
                        it.protocol.name.lowercase() == protocol.lowercase()
            }.groupBy {
                it.network
            }

        if (fetchersByNetwork.isEmpty()) {
            return@coroutineScope emptyList()
        }

        data class RewardAndClaimable(
            val reward: Reward,
            val claimable: ClaimableMarket,
            val claimableRewardFetcher: ClaimableRewardFetcher
        )

        return@coroutineScope fetchersByNetwork.map { marketsByNetwork ->
            async {
                try {
                    val rewards = marketsByNetwork.value.flatMap { claimable ->
                        claimable.claimableRewardFetchers.flatMap { fetcher ->
                            fetcher.rewards.map { reward ->
                                RewardAndClaimable(reward, claimable, fetcher)
                            }
                        }
                    }

                    blockchainGatewayProvider.getGateway(marketsByNetwork.key).readMultiCall(
                        rewards.map { it.reward.getRewardFunction(userAddress) }
                    ).mapIndexed { index, retVal ->
                        async {
                            catch {
                                if (retVal.success) {
                                    val reward = rewards[index]
                                    val earned = reward.reward.extractAmountFromRewardFunction(retVal.data, userAddress)

                                    if (earned > BigInteger.ONE) {
                                        Some(
                                            UserClaimable(
                                                id = reward.claimable.id,
                                                name = reward.claimable.name,
                                                protocol = reward.claimable.protocol,
                                                network = reward.claimable.network,
                                                amount = earned,
                                                claimableToken = reward.reward.token,
                                                claimTransaction = reward.claimableRewardFetcher.preparedTransaction.invoke(
                                                    userAddress
                                                )
                                            ),
                                        )
                                    } else {
                                        None
                                    }
                                } else {
                                    None
                                }
                            }
                        }
                    }.awaitAll().map {
                        it.getOrNone().flatten()
                    }.mapNotNull(Option<UserClaimable>::getOrNull)
                } catch (ex: Exception) {
                    logger.info("Unable to fetch claimables for ${marketsByNetwork.key}", ex)
                    emptyList()
                }
            }
        }.awaitAll().flatten()
    }
}