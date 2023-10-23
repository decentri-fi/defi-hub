package io.defitrack.protocol.velodrome.pooling

import arrow.core.Either
import arrow.core.flatMap
import arrow.core.nonEmptyListOf
import arrow.fx.coroutines.parMapNotNull
import io.defitrack.common.network.Network
import io.defitrack.common.utils.BigDecimalExtensions.dividePrecisely
import io.defitrack.common.utils.FormatUtilsExtensions.asEth
import io.defitrack.common.utils.Refreshable.Companion.refreshable
import io.defitrack.conditional.ConditionalOnCompany
import io.defitrack.erc20.TokenInformationVO
import io.defitrack.market.farming.domain.FarmingMarket
import io.defitrack.market.pooling.PoolingMarketProvider
import io.defitrack.market.pooling.domain.PoolingMarket
import io.defitrack.market.pooling.domain.PoolingMarketTokenShare
import io.defitrack.price.PriceRequest
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
import kotlinx.coroutines.launch
import net.bytebuddy.implementation.bytecode.Throw
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component
import java.math.BigDecimal
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.coroutines.coroutineContext

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

        pairFactoryContract.allPools().parMapNotNull(concurrency = 12) {
            Either.catch {
                createMarket(it)
            }.mapLeft { throwable ->
                logger.error("Error creating market for address {}", it)
            }.getOrNull()
        }.forEach {
            send(it)
        }
    }

    private suspend fun createMarket(it: String): PoolingMarket = coroutineScope {
        val poolingToken = getToken(it)
        val contract = VelodromePoolContract(
            getBlockchainGateway(), it
        )

        val reserves = contract.getReserves()

        val share0 = async {
            val token0 = getToken(contract.token0.await())
            val amount0 = reserves.amount0
            PoolingMarketTokenShare(
                token0,
                amount0,
                getPriceResource().calculatePrice(
                    PriceRequest(token0.address, getNetwork(), amount0.asEth(token0.decimals))
                ).toBigDecimal()
            )
        }

        val share1 = async {
            val token1 = getToken(contract.token1.await())
            val amount1 = reserves.amount1
            PoolingMarketTokenShare(
                token1,
                amount1,
                getPriceResource().calculatePrice(
                    PriceRequest(token1.address, getNetwork(), amount1.asEth(token1.decimals))
                ).toBigDecimal()
            )
        }

        val breakdown = nonEmptyListOf(
            share0, share1
        ).awaitAll()

        create(
            identifier = "v2-$it",
            marketSize = refreshable(breakdown.sumOf { it.reserveUSD }) {
                val token0 = getToken(contract.token0.await())
                val token1 = getToken(contract.token1.await())

                val contract = VelodromePoolContract(
                    getBlockchainGateway(), it
                )
                val amount0 = contract.getReserves().amount0
                val amount1 = contract.getReserves().amount1
                nonEmptyListOf(
                    PoolingMarketTokenShare(
                        token0,
                        amount0,
                        getPriceResource().calculatePrice(
                            PriceRequest(token0.address, getNetwork(), amount0.asEth(token0.decimals))
                        ).toBigDecimal()
                    ),
                    PoolingMarketTokenShare(
                        token0,
                        amount0,
                        getPriceResource().calculatePrice(
                            PriceRequest(token1.address, getNetwork(), amount1.asEth(token0.decimals))
                        ).toBigDecimal()
                    )
                ).sumOf { it.reserveUSD }
            },
            positionFetcher = defaultPositionFetcher(poolingToken.address),
            address = it,
            name = poolingToken.name,
            breakdown = breakdown,
            symbol = poolingToken.symbol,
            tokens = poolingToken.underlyingTokens.map(TokenInformationVO::toFungibleToken),
            totalSupply = refreshable(poolingToken.totalSupply.asEth(poolingToken.decimals)) {
                getToken(it).totalDecimalSupply()
            },
            deprecated = false,
        )
    }


    override fun getProtocol(): Protocol {
        return Protocol.VELODROME_V2
    }

    override fun getNetwork(): Network {
        return Network.OPTIMISM
    }
}