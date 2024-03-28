package io.defitrack.protocol.application.set

import io.defitrack.adapter.output.domain.market.GetPriceCommand
import io.defitrack.common.network.Network
import io.defitrack.common.utils.BigDecimalExtensions.dividePrecisely
import io.defitrack.common.utils.refreshable
import io.defitrack.architecture.conditional.ConditionalOnCompany
import io.defitrack.architecture.conditional.ConditionalOnNetwork
import io.defitrack.common.utils.FormatUtilsExtensions.asEth
import io.defitrack.market.port.out.PoolingMarketProvider
import io.defitrack.market.domain.PoolingMarket
import io.defitrack.market.domain.PoolingMarketTokenShare
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
                    val tokenContract = createContract { SetTokenContract(it) }

                    val token = getToken(it)

                    val breakdown = refreshable {
                        tokenContract.getPositions().map {
                            val supply = tokenContract.totalSupply().get().asEth(tokenContract.readDecimals())
                            val underlying = getToken(it.token)
                            val reserve = it.amount.toBigDecimal()
                                .times(tokenContract.getPositionMultiplier().asEth())
                                .times(supply).toBigInteger()
                            PoolingMarketTokenShare(
                                token = underlying,
                                reserve = reserve,
                            )
                        }
                    }

                    create(
                        identifier = it,
                        address = it,
                        name = token.name,
                        symbol = token.symbol,
                        breakdown = breakdown,
                        positionFetcher = defaultPositionFetcher(it),
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

    override fun getNetwork(): Network {
        return Network.POLYGON
    }
}