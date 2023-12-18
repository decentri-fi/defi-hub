package io.defitrack.protocol.quickswap.pooling

import arrow.core.Either.Companion.catch
import arrow.fx.coroutines.parMapNotNull
import io.defitrack.common.network.Network
import io.defitrack.common.utils.FormatUtilsExtensions.asEth
import io.defitrack.common.utils.map
import io.defitrack.common.utils.refreshable
import io.defitrack.conditional.ConditionalOnCompany
import io.defitrack.market.pooling.PoolingMarketProvider
import io.defitrack.market.pooling.domain.PoolingMarket
import io.defitrack.market.pooling.domain.PoolingMarketTokenShare
import io.defitrack.protocol.Company
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.quickswap.QuickswapService
import io.defitrack.protocol.quickswap.domain.QuickswapPair
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import org.springframework.stereotype.Component
import kotlin.coroutines.EmptyCoroutineContext

@Component
@ConditionalOnCompany(Company.QUICKSWAP)
class QuickswapPoolingMarketProvider(
    private val quickswapService: QuickswapService,
) : PoolingMarketProvider() {

    override suspend fun produceMarkets(): Flow<PoolingMarket> = channelFlow {
        quickswapService.getPairs().parMapNotNull(EmptyCoroutineContext, 8) {
            catch {
                toPoolingMarket(it)
            }.mapLeft {
                logger.error("Error while fetching Quickswap market", it)
                null
            }.getOrNull()
        }.forEach {
            send(it)
        }
    }

    private suspend fun toPoolingMarket(it: QuickswapPair): PoolingMarket? {
        val token0 = getToken(it.token0.id)
        val token1 = getToken(it.token1.id)
        if (token0.symbol == "UNKWN" || token1.symbol == "UNKWN") return null

        val token = getToken(it.id)

        val breakdown = refreshable {
            fiftyFiftyBreakdown(token0, token1, token.address)
        }

        return create(
            address = it.id,
            identifier = it.id,
            name = token.name,
            symbol = token.symbol,
            tokens = listOf(token0, token1),
            breakdown = breakdown,
            marketSize = breakdown.map {
                it.sumOf(PoolingMarketTokenShare::reserveUSD)
            },
            positionFetcher = defaultPositionFetcher(token.address),
            totalSupply = refreshable{
                getToken(it.id).totalSupply.asEth(token.decimals)
            }
        )
    }

    override fun getProtocol(): Protocol {
        return Protocol.QUICKSWAP
    }

    override fun getNetwork(): Network {
        return Network.POLYGON
    }
}