package io.defitrack.protocol.velodrome.pooling

import arrow.core.Either
import arrow.core.Either.Companion.catch
import arrow.core.nonEmptyListOf
import arrow.fx.coroutines.parMapNotNull
import io.defitrack.common.network.Network
import io.defitrack.common.utils.refreshable
import io.defitrack.architecture.conditional.ConditionalOnCompany
import io.defitrack.common.utils.FormatUtilsExtensions.asEth
import io.defitrack.market.port.out.PoolingMarketProvider
import io.defitrack.market.domain.PoolingMarket
import io.defitrack.market.domain.PoolingMarketTokenShare
import io.defitrack.protocol.Company
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.velodrome.VelodromeOptimismService
import io.defitrack.protocol.velodrome.contract.PoolFactoryContract
import io.defitrack.protocol.velodrome.contract.VelodromePoolContract
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component

@Component
@ConditionalOnCompany(Company.VELODROME)
@ConditionalOnProperty(value = ["velodromev2.enabled"], havingValue = "true", matchIfMissing = true)
class VelodromeV2OptimismPoolingMarketProvider(
    private val velodromeOptimismService: VelodromeOptimismService
) : PoolingMarketProvider() {

    override suspend fun produceMarkets(): Flow<PoolingMarket> = channelFlow {
        val pairFactoryContract = PoolFactoryContract(
            blockchainGateway = getBlockchainGateway(),
            contractAddress = velodromeOptimismService.getV2PoolFactory()
        )

        pairFactoryContract.allPools()
            .map(::velodromeContract)
            .resolve()
            .parMapNotNull(concurrency = 12) {
                catch {
                    createMarket(it)
                }.mapLeft { exc ->
                    logger.error("Error creating market for address {}: {}  ", it, exc.message)
                }.getOrNull()
            }.forEach {
                send(it)
            }
    }

    private fun velodromeContract(it: String) = with(getBlockchainGateway()) { VelodromePoolContract(it) }

    private suspend fun createMarket(contract: VelodromePoolContract): PoolingMarket = coroutineScope {
        val poolingToken = getToken(contract.address)


        val breakdown = refreshable {
            val reserves = contract.reserves()

            nonEmptyListOf(
                async {
                    val token0 = getToken(contract.token0.await())
                    val amount0 = reserves.amount0
                    PoolingMarketTokenShare(
                        token0,
                        amount0,
                    )
                },
                async {
                    val token1 = getToken(contract.token1.await())
                    val amount1 = reserves.amount1
                    PoolingMarketTokenShare(
                        token1,
                        amount1,
                    )
                }
            ).awaitAll()
        }

        create(
            identifier = "v2-${contract.address}",
            positionFetcher = defaultPositionFetcher(poolingToken.address),
            address = contract.address,
            name = poolingToken.name,
            breakdown = breakdown,
            symbol = poolingToken.symbol,
            tokens = poolingToken.underlyingTokens,
            totalSupply = refreshable {
                contract.totalSupply().asEth(poolingToken.decimals)
            },
            deprecated = false,
            metadata = mapOf(
                "stable" to contract.stable.await(),
            )
        )
    }


    override fun getProtocol(): Protocol {
        return Protocol.VELODROME_V2
    }

    override fun getNetwork(): Network {
        return Network.OPTIMISM
    }
}