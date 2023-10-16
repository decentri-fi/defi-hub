package io.defitrack.claimable

import arrow.core.*
import arrow.core.Either.Companion.catch
import io.defitrack.claimable.domain.UserClaimable
import io.defitrack.evm.contract.BlockchainGatewayProvider
import io.defitrack.protocol.Protocol
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.math.BigInteger
import kotlin.collections.flatten

@Component
class DefaultUserClaimableProvider(
    private val claimableMarketProviders: List<ClaimableMarketProvider>,
    private val blockchainGatewayProvider: BlockchainGatewayProvider
) {

    private val logger = LoggerFactory.getLogger(this::class.java)
    suspend fun claimables(userAddress: String, protocol: String): List<UserClaimable> = coroutineScope {
        val fetchersByNetwork = claimableMarketProviders.flatMap { it.getMarkets() }
            .filter {
                it.protocol.slug.lowercase() == protocol.lowercase() ||
                        it.protocol.name.lowercase() == protocol.lowercase()
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
                        rewards.map { it.first.toMulticall(userAddress) }
                    ).mapIndexed { index, retVal ->
                        catch {
                            if (retVal.success) {
                                val reward = rewards[index]
                                val earned = reward.first.extractAmountFromRewardFunction(retVal.data, userAddress)

                                if (earned > BigInteger.ONE) {
                                    Some(
                                        UserClaimable(
                                            id = reward.second.id,
                                            name = reward.second.name,
                                            protocol = reward.second.protocol,
                                            network = reward.second.network,
                                            amount = earned,
                                            claimableToken = reward.first.token,
                                            claimTransaction = reward.second.claimableRewardFetcher.preparedTransaction.invoke(
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
                    }.map {
                        it.getOrNone().flatten()
                    }.mapNotNull(Option<UserClaimable>::getOrNull)
                } catch (ex: Exception) {
                    logger.info("Unable to fetch claimables for ${entry.key}", ex)
                    emptyList()
                }
            }
        }.awaitAll().flatten()
    }
}