package io.defitrack.market.farming

import arrow.core.Either.Companion.catch
import arrow.core.None
import arrow.core.some
import io.defitrack.evm.contract.BlockchainGatewayProvider
import io.defitrack.market.farming.domain.FarmingPosition
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.math.BigInteger

@Service
class DefaultFarmingPositionProvider(
    val farmingMarketProvider: List<FarmingMarketProvider>,
    val gateway: BlockchainGatewayProvider
) : FarmingPositionProvider() {

    val logger = LoggerFactory.getLogger(this::class.java)

    val semaphore = Semaphore(16)

    override suspend fun getStakings(protocol: String, address: String): List<FarmingPosition> = coroutineScope {

        farmingMarketProvider.filter {
            it.getProtocol().slug == protocol
        }.flatMap { provider ->
            val markets = provider.getMarkets().filter { it.balanceFetcher != null }
            if (markets.isEmpty()) {
                return@flatMap emptyList()
            }

            gateway.getGateway(provider.getNetwork()).readMultiCall(
                markets.map { market ->
                    market.balanceFetcher!!.functionCreator(address)
                }
            ).mapIndexed { index, retVal ->
                async {
                    semaphore.withPermit {
                        val market = markets[index]
                        catch {

                            if (!retVal.success) {
                                logger.info("Call to get position returned error ${market.name}")
                                return@catch None
                            }

                            val balance = market.balanceFetcher!!.extractBalance(retVal.data)

                            if (balance.underlyingAmount > BigInteger.ONE) {
                                FarmingPosition(
                                    market,
                                    balance.underlyingAmount,
                                    balance.tokenAmount
                                ).some()
                            } else {
                                None
                            }
                        }.mapLeft {
                            logger.error("Error fetching balance for ${market.name}: {}", it.message)
                        }.map {
                            it.getOrNull()
                        }
                    }
                }
            }.awaitAll().mapNotNull {
                it.getOrNull()
            }
        }
    }
}