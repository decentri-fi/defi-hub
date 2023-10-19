package io.defitrack.protocol.velodrome.pooling

import arrow.core.Either
import arrow.core.flatMap
import arrow.fx.coroutines.parMapNotNull
import io.defitrack.common.network.Network
import io.defitrack.common.utils.FormatUtilsExtensions.asEth
import io.defitrack.common.utils.Refreshable.Companion.refreshable
import io.defitrack.conditional.ConditionalOnCompany
import io.defitrack.erc20.TokenInformationVO
import io.defitrack.market.farming.domain.FarmingMarket
import io.defitrack.market.pooling.PoolingMarketProvider
import io.defitrack.market.pooling.domain.PoolingMarket
import io.defitrack.protocol.Company
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.velodrome.VelodromeOptimismService
import io.defitrack.protocol.velodrome.contract.PoolFactoryContract
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.launch
import net.bytebuddy.implementation.bytecode.Throw
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component
import java.math.BigDecimal
import kotlin.coroutines.EmptyCoroutineContext

@Component
@ConditionalOnCompany(Company.VELODROME)
@ConditionalOnProperty(value = ["velodromev2.enabled"], havingValue = "true", matchIfMissing = true)
class VelodromeV2OptimismPoolingMarketProvider(
    private val velodromeOptimismService: VelodromeOptimismService
) : PoolingMarketProvider() {

    override suspend fun produceMarkets(): Flow<PoolingMarket> = channelFlow {
        val pairFactoryContract = PoolFactoryContract(
            blockchainGateway = getBlockchainGateway(),
            contractAddress = velodromeOptimismService.getV2PoolFactory()
        )

        pairFactoryContract.allPools().parMapNotNull(EmptyCoroutineContext, 12) {
            createMarket(it)
        }.mapNotNull {
            it.mapLeft { throwable ->
                logger.error("Error creating market", throwable)
            }.getOrNull()
        }.forEach {
            send(it)
        }
    }

    private suspend fun createMarket(it: String): Either<Throwable, PoolingMarket> {
        return Either.catch {
            val poolingToken = getToken(it)
            val tokens = poolingToken.underlyingTokens.map(TokenInformationVO::toFungibleToken)

            val breakdown = fiftyFiftyBreakdown(tokens[0], tokens[1], poolingToken.address)
            create(
                identifier = "v2-$it",
                marketSize = refreshable(breakdown.sumOf { it.reserveUSD }) {
                    fiftyFiftyBreakdown(tokens[0], tokens[1], poolingToken.address).sumOf { it.reserveUSD }
                },
                positionFetcher = defaultPositionFetcher(poolingToken.address),
                address = it,
                name = poolingToken.name,
                breakdown = breakdown,
                symbol = poolingToken.symbol,
                tokens = poolingToken.underlyingTokens.map(TokenInformationVO::toFungibleToken),
                totalSupply = refreshable(poolingToken.totalSupply.asEth(poolingToken.decimals)) {
                    getToken(it).totalDecimalSupply()
                },
                deprecated = false,
                price = refreshable {
                    BigDecimal.ZERO
                }
            )
        }
    }


    override fun getProtocol(): Protocol {
        return Protocol.VELODROME_V2
    }

    override fun getNetwork(): Network {
        return Network.OPTIMISM
    }
}