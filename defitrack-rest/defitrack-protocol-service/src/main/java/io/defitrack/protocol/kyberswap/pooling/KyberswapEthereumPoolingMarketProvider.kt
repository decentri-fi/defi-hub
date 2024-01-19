package io.defitrack.protocol.kyberswap.pooling

import arrow.core.Either.Companion.catch
import arrow.fx.coroutines.parMapNotNull
import io.defitrack.common.network.Network
import io.defitrack.common.utils.refreshable
import io.defitrack.architecture.conditional.ConditionalOnCompany
import io.defitrack.market.port.out.PoolingMarketProvider
import io.defitrack.market.domain.PoolingMarket
import io.defitrack.protocol.Company
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.kyberswap.apr.KyberswapAPRService
import io.defitrack.protocol.kyberswap.graph.KyberswapEthereumGraphProvider
import io.defitrack.protocol.kyberswap.graph.domain.Pool
import org.springframework.stereotype.Component

@Component
@ConditionalOnCompany(Company.KYBER_SWAP)
class KyberswapEthereumPoolingMarketProvider(
    private val kyberswapPolygonService: KyberswapEthereumGraphProvider,
    private val kyberswapAPRService: KyberswapAPRService,
) : PoolingMarketProvider() {

    override suspend fun fetchMarkets(): List<PoolingMarket> {
        return kyberswapPolygonService.getPoolingMarkets().parMapNotNull(concurrency = 8) { pool ->
            catch {
                createMarket(pool)
            }.mapLeft {
                logger.error("Unable to get pooling market {}: {}", pool.id, it.message)
                null
            }.getOrNull()
        }
    }

    private suspend fun createMarket(it: Pool): PoolingMarket {
        val token = getToken(it.id)
        val token0 = getToken(it.token0.id)
        val token1 = getToken(it.token1.id)

        return create(
            identifier = it.id,
            address = it.id,
            name = token.name,
            symbol = token.symbol,
            tokens = listOf(token0, token1),
            apr = kyberswapAPRService.getAPR(it.pair.id, getNetwork()),
            marketSize = refreshable(it.reserveUSD),
            positionFetcher = defaultPositionFetcher(token.address),
            totalSupply = refreshable {
                getToken(it.id).totalDecimalSupply()
            }
        )
    }

    override fun getProtocol(): Protocol = Protocol.KYBER_SWAP

    override fun getNetwork(): Network = Network.ETHEREUM
}