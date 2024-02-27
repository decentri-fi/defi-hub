package io.defitrack.protocol.hop.pooling

import arrow.core.Either
import arrow.core.nonEmptyListOf
import arrow.fx.coroutines.parMapNotNull
import io.defitrack.common.utils.FormatUtilsExtensions.asEth
import io.defitrack.common.utils.map
import io.defitrack.common.utils.refreshable
import io.defitrack.price.domain.GetPriceCommand
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.market.port.out.PoolingMarketProvider
import io.defitrack.market.domain.PoolingMarket
import io.defitrack.market.domain.PoolingMarketTokenShare
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

    override suspend fun produceMarkets(): Flow<PoolingMarket> = channelFlow {
        hopService.getLps(getNetwork()).parMapNotNull(concurrency = 12) { hopLpToken ->
            toPoolingMarketElement(getBlockchainGateway(), hopLpToken).mapLeft {
                logger.error("Unable to get pooling market: {}", it.message)
            }.getOrNull()
        }.forEach {
            send(it)
        }
    }

    private suspend fun toPoolingMarketElement(
        gateway: BlockchainGateway, hopLpToken: HopLpToken
    ): Either<Throwable, PoolingMarket> {
        return Either.catch {
            val contract = getHopLpContract(hopLpToken)

            val lp = getToken(hopLpToken.lpToken)

            val swapContract = HopSwapContract(
                blockchainGateway = gateway, contract.swap()
            )

            val htoken = getToken(hopLpToken.hToken)
            val canonical = getToken(hopLpToken.canonicalToken)

            create(identifier = hopLpToken.canonicalToken,
                address = hopLpToken.lpToken,
                symbol = htoken.symbol + "-" + canonical.symbol,
                name = lp.name,
                tokens = nonEmptyListOf(htoken, canonical),
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
                totalSupply = contract.totalSupply().map { it.asEth(lp.decimals) })
        }
    }

    private fun getHopLpContract(hopLpToken: HopLpToken) = with(getBlockchainGateway()) {
        HopLpTokenContract(hopLpToken.lpToken)
    }

    override fun getProtocol(): Protocol {
        return Protocol.HOP
    }
}