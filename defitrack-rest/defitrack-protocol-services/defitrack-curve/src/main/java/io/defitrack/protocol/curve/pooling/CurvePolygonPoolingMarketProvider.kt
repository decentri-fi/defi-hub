package io.defitrack.protocol.curve.pooling

import io.defitrack.common.network.Network
import io.defitrack.market.pooling.PoolingMarketProvider
import io.defitrack.market.pooling.domain.PoolingMarketElement
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.crv.CurvePolygonGraphProvider
import io.defitrack.token.ERC20Resource
import io.defitrack.token.TokenType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import org.springframework.stereotype.Component
import java.math.BigInteger


@Component
class CurvePolygonPoolingMarketProvider(
    private val curvePolygonGraphProvider: CurvePolygonGraphProvider,
    private val erC20Resource: ERC20Resource
) : PoolingMarketProvider() {

    override suspend fun fetchPoolingMarkets(): List<PoolingMarketElement> =
        withContext(Dispatchers.IO.limitedParallelism(10)) {
            curvePolygonGraphProvider.getPools()
                .filter {
                    it.pool.balances.reduce { a, b ->
                        a.plus(b)
                    } > BigInteger.ZERO
                }
                .map {
                    async {
                        try {
                            val lp = erC20Resource.getTokenInformation(getNetwork(), it.id)

                            val underlying = it.pool.coins.map { coin ->
                                erC20Resource.getTokenInformation(getNetwork(), coin.id)
                            }

                            PoolingMarketElement(
                                id = "crv-polygon-${it.id}",
                                network = getNetwork(),
                                protocol = getProtocol(),
                                address = it.id,
                                name = lp.name,
                                symbol = lp.symbol,
                                tokens = underlying.map { u -> u.toFungibleToken() },
                                tokenType = TokenType.CURVE
                            )
                        } catch (ex: Exception) {
                            null
                        }
                    }
                }.awaitAll().filterNotNull()
        }

    override fun getProtocol(): Protocol {
        return Protocol.CURVE
    }

    override fun getNetwork(): Network {
        return Network.POLYGON
    }
}