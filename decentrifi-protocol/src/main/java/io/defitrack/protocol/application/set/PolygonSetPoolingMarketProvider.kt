package io.defitrack.protocol.application.set

import io.defitrack.common.network.Network
import io.defitrack.common.utils.BigDecimalExtensions.dividePrecisely
import io.defitrack.common.utils.refreshable
import io.defitrack.architecture.conditional.ConditionalOnCompany
import io.defitrack.architecture.conditional.ConditionalOnNetwork
import io.defitrack.price.domain.GetPriceCommand
import io.defitrack.market.port.out.PoolingMarketProvider
import io.defitrack.market.domain.PoolingMarket
import io.defitrack.protocol.Company
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.set.PolygonSetProvider
import io.defitrack.protocol.set.SetTokenContract
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.springframework.stereotype.Component
import java.math.BigDecimal

@Component
@ConditionalOnCompany(Company.SET)
@ConditionalOnNetwork(Network.POLYGON)
class PolygonSetPoolingMarketProvider(
    private val polygonSetProvider: PolygonSetProvider,
) : PoolingMarketProvider() {

    override suspend fun fetchMarkets(): List<PoolingMarket> = coroutineScope {
        return@coroutineScope polygonSetProvider.getSets().map {
            async {
                try {

                    val tokenContract = with(getBlockchainGateway()) {  SetTokenContract(it) }

                    val token = getToken(it)

                    val positions = tokenContract.getPositions()

                    create(
                        identifier = it,
                        address = it,
                        name = token.name,
                        symbol = token.symbol,
                        tokens = positions.map {
                            getToken(it.token)
                        },
                        apr = null,
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
                GetPriceCommand(
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