package io.defitrack.protocol.application.hop.pooling

import arrow.core.Either.Companion.catch
import arrow.fx.coroutines.parMapNotNull
import io.defitrack.common.utils.map
import io.defitrack.common.utils.refreshable
import io.defitrack.market.domain.PoolingMarket
import io.defitrack.market.domain.PoolingMarketTokenShare
import io.defitrack.market.port.out.PoolingMarketProvider
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.hop.HopService
import io.defitrack.protocol.hop.contract.HopLpTokenContract
import io.defitrack.protocol.hop.contract.HopSwapContract
import io.defitrack.protocol.hop.domain.HopLpToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import java.math.BigDecimal

abstract class HopPoolingMarketProvider(
    private val hopService: HopService,
) : PoolingMarketProvider() {

    override suspend fun produceMarkets() = channelFlow {
        hopService.getLps(getNetwork()).parMapNotNull(concurrency = 12) { hopLpToken ->
            createMarket(hopLpToken)
        }.forEach {
            send(it)
        }
    }

    private suspend fun createMarket(
        hopLpToken: HopLpToken
    ): PoolingMarket? {
        return catch {
            val contract = createContract {
                HopLpTokenContract(hopLpToken.lpToken)
            }

            val lp = getToken(hopLpToken.lpToken)

            val swapContract = createContract { HopSwapContract(contract.swap()) }

            val htoken = getToken(hopLpToken.hToken)
            val canonical = getToken(hopLpToken.canonicalToken)

            create(
                identifier = hopLpToken.canonicalToken,
                address = hopLpToken.lpToken,
                symbol = htoken.symbol + "-" + canonical.symbol,
                name = lp.name,
                breakdown = refreshable {
                    val tokenAmount = contract.totalSupply().map {
                        it.toBigDecimal().times(swapContract.virtualPrice().toBigDecimal())
                            .divide(BigDecimal.TEN.pow(36))
                    }

                    listOf(
                        PoolingMarketTokenShare(
                            canonical, tokenAmount.map {
                                it.times(BigDecimal.TEN.pow(canonical.decimals)).toBigInteger()
                            }.get()
                        )
                    )
                },
                positionFetcher = defaultPositionFetcher(hopLpToken.lpToken),
                totalSupply = refreshable(lp.totalDecimalSupply()) {
                    getToken(hopLpToken.lpToken).totalDecimalSupply()
                }
            )
        }.mapLeft {
            logger.error("Unable to get pooling market: {}", it.message)
        }.getOrNull()
    }


    override fun getProtocol(): Protocol {
        return Protocol.HOP
    }
}