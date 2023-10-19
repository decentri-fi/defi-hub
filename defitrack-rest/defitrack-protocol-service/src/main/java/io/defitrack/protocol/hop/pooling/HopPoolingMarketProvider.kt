package io.defitrack.protocol.hop.pooling

import arrow.core.Either
import arrow.fx.coroutines.parMapNotNull
import io.defitrack.common.utils.FormatUtilsExtensions.asEth
import io.defitrack.common.utils.Refreshable.Companion.refreshable
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.market.pooling.PoolingMarketProvider
import io.defitrack.market.pooling.domain.PoolingMarket
import io.defitrack.price.PriceRequest
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.hop.HopService
import io.defitrack.protocol.hop.contract.HopLpTokenContract
import io.defitrack.protocol.hop.contract.HopSwapContract
import io.defitrack.protocol.hop.domain.HopLpToken
import io.defitrack.token.TokenType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import java.math.BigDecimal
import kotlin.coroutines.EmptyCoroutineContext

abstract class HopPoolingMarketProvider(
    private val hopService: HopService,
) : PoolingMarketProvider() {

    override suspend fun produceMarkets(): Flow<PoolingMarket> = channelFlow {
        hopService.getLps(getNetwork()).parMapNotNull(EmptyCoroutineContext, 12) { hopLpToken ->
            toPoolingMarketElement(getBlockchainGateway(), hopLpToken)
        }.mapNotNull {
            it.mapLeft {
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

            val swapContract = HopSwapContract(
                blockchainGateway = gateway,
                contract.swap()
            )

            val htoken = getToken(hopLpToken.hToken)
            val canonical = getToken(hopLpToken.canonicalToken)

            val marketSize = getPrice(canonical.address, contract, swapContract).toBigDecimal()
            create(
                identifier = hopLpToken.canonicalToken,
                address = hopLpToken.lpToken,
                symbol = htoken.symbol + "-" + canonical.symbol,
                name = contract.name(),
                tokens = listOf(
                    htoken.toFungibleToken(),
                    canonical.toFungibleToken()
                ),
                marketSize = refreshable(marketSize) {
                    getPrice(canonical.address, contract, swapContract).toBigDecimal()
                },
                positionFetcher = defaultPositionFetcher(hopLpToken.lpToken),
                totalSupply = refreshable {
                    contract.totalSupply().asEth(contract.decimals())
                }
            )
        }
    }

    private suspend fun getPrice(
        canonicalTokenAddress: String,
        contract: HopLpTokenContract,
        swapContract: HopSwapContract
    ): Double {

        val tokenAmount = contract.totalSupply().toBigDecimal().times(
            swapContract.virtualPrice().toBigDecimal()
        ).divide(BigDecimal.TEN.pow(36))

        return getPriceResource().calculatePrice(
            PriceRequest(
                address = canonicalTokenAddress,
                network = getNetwork(),
                amount = tokenAmount,
                TokenType.SINGLE
            )
        )
    }


    override fun getProtocol(): Protocol {
        return Protocol.HOP
    }
}