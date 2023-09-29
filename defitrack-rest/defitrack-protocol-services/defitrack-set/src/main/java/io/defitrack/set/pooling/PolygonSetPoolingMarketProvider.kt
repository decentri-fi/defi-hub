package io.defitrack.set.pooling

import io.defitrack.common.network.Network
import io.defitrack.common.utils.BigDecimalExtensions.dividePrecisely
import io.defitrack.common.utils.FormatUtilsExtensions.asEth
import io.defitrack.common.utils.Refreshable.Companion.refreshable
import io.defitrack.market.pooling.PoolingMarketProvider
import io.defitrack.market.pooling.domain.PoolingMarket
import io.defitrack.price.PriceRequest
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.set.PolygonSetProvider
import io.defitrack.protocol.set.SetTokenContract
import io.defitrack.token.TokenType
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.springframework.stereotype.Service
import java.math.BigDecimal

@Service
class PolygonSetPoolingMarketProvider(
    private val polygonSetProvider: PolygonSetProvider,
) : PoolingMarketProvider() {

    override suspend fun fetchMarkets(): List<PoolingMarket> = coroutineScope {
        return@coroutineScope polygonSetProvider.getSets().map {
            async {
                try {

                    val tokenContract = SetTokenContract(
                        getBlockchainGateway(), it
                    )

                    val token = getToken(it)

                    val positions = tokenContract.getPositions()


                    create(
                        identifier = it,
                        address = it,
                        name = token.name,
                        symbol = token.symbol,
                        tokens = positions.map {
                            getToken(it.token).toFungibleToken()
                        },
                        apr = null,
                        marketSize = refreshable {
                            val price = getPrice(positions)
                            val supply = token.totalSupply.asEth(token.decimals)
                            price.times(supply)
                        },
                        positionFetcher = defaultPositionFetcher(it),
                        investmentPreparer = null,
                        totalSupply = refreshable(token.totalDecimalSupply()) {
                            getToken(it).totalDecimalSupply()
                        }
                    )
                } catch (ex: Exception) {
                    logger.error("Unable to import set with address $it", ex)
                    null
                }
            }
        }.awaitAll().filterNotNull()
    }

    override fun getProtocol(): Protocol {
        return Protocol.SET
    }

    suspend fun getPrice(positions: List<SetTokenContract.Position>): BigDecimal {
        val price = positions.sumOf {
            val token = getToken(it.token)

            getPriceResource().calculatePrice(
                PriceRequest(
                    it.token,
                    getNetwork(),
                    it.amount.dividePrecisely(BigDecimal.TEN.pow(token.decimals)),
                    token.type
                )
            )
        }.toBigDecimal()

        return if (price <= BigDecimal.ZERO) {
            BigDecimal.ZERO
        } else price
    }

    override fun getNetwork(): Network {
        return Network.POLYGON
    }
}