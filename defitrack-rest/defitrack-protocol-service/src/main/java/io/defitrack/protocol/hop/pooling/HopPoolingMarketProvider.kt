package io.defitrack.protocol.hop.pooling

import arrow.core.Either
import arrow.core.nonEmptyListOf
import arrow.fx.coroutines.parMapNotNull
import io.defitrack.common.utils.FormatUtilsExtensions.asEth
import io.defitrack.common.utils.map
import io.defitrack.common.utils.refreshable
import io.defitrack.domain.GetPriceCommand
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.market.pooling.PoolingMarketProvider
import io.defitrack.market.pooling.domain.PoolingMarket
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
            toPoolingMarketElement(getBlockchainGateway(), hopLpToken)
                .mapLeft {
                    logger.error("Unable to get pooling market: {}", it.message)
                }.getOrNull()
        }.forEach {
            send(it)
        }
    }

    private suspend fun toPoolingMarketElement(
        gateway: BlockchainGateway,
        hopLpToken: HopLpToken
    ): Either<Throwable, PoolingMarket> {
        return Either.catch {
            val contract = HopLpTokenContract(
                blockchainGateway = gateway,
                hopLpToken.lpToken
            )

            val lp = getToken(hopLpToken.lpToken)

            val swapContract = HopSwapContract(
                blockchainGateway = gateway,
                contract.swap()
            )

            val htoken = getToken(hopLpToken.hToken)
            val canonical = getToken(hopLpToken.canonicalToken)

            create(
                identifier = hopLpToken.canonicalToken,
                address = hopLpToken.lpToken,
                symbol = htoken.symbol + "-" + canonical.symbol,
                name = lp.name,
                tokens = nonEmptyListOf(htoken, canonical),
                marketSize = refreshable {
                    getPrice(canonical.address, contract, swapContract).toBigDecimal()
                },
                positionFetcher = defaultPositionFetcher(hopLpToken.lpToken),
                totalSupply = contract.totalSupply()
                    .map { it.asEth(lp.decimals) }
            )
        }
    }

    private suspend fun getPrice(
        canonicalTokenAddress: String,
        contract: HopLpTokenContract,
        swapContract: HopSwapContract
    ): Double {

        val tokenAmount = contract.totalSupply().map {
            it.toBigDecimal().times(swapContract.virtualPrice().toBigDecimal()).divide(BigDecimal.TEN.pow(36))
        }

        return getPriceResource().calculatePrice(
            GetPriceCommand(
                address = canonicalTokenAddress,
                network = getNetwork(),
                amount = tokenAmount.get(),
            )
        )
    }


    override fun getProtocol(): Protocol {
        return Protocol.HOP
    }
}