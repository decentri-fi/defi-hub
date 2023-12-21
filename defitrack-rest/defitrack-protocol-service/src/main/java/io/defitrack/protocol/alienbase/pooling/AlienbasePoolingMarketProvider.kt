package io.defitrack.protocol.alienbase.pooling

import arrow.core.Either
import arrow.core.Either.Companion.catch
import arrow.fx.coroutines.parMap
import arrow.fx.coroutines.parMapNotNull
import io.defitrack.common.network.Network
import io.defitrack.common.utils.refreshable
import io.defitrack.conditional.ConditionalOnCompany
import io.defitrack.market.pooling.PoolingMarketProvider
import io.defitrack.market.pooling.domain.PoolingMarket
import io.defitrack.protocol.Company
import io.defitrack.protocol.Protocol
import io.defitrack.uniswap.v2.PairFactoryContract
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import org.springframework.stereotype.Component

@Component
@ConditionalOnCompany(Company.ALIENBASE)
class AlienbasePoolingMarketProvider : PoolingMarketProvider() {

    private val poolFactoryAddress: String = "0x3e84d913803b02a4a7f027165e8ca42c14c0fde7"

    override suspend fun produceMarkets(): Flow<PoolingMarket> = channelFlow {
        PairFactoryContract(
            blockchainGateway = getBlockchainGateway(),
            contractAddress = poolFactoryAddress
        ).allPairs().parMapNotNull(concurrency = 12) { pair ->
            catch {
                createMarket(pair)
            }.mapLeft {
                logger.info("Unable to create market {}", pair)
            }.getOrNull()
        }.forEach {
            send(it)
        }
    }

    private suspend fun AlienbasePoolingMarketProvider.createMarket(it: String): PoolingMarket {
        val poolingToken = getToken(it)
        val tokens = poolingToken.underlyingTokens

        val breakdown = refreshable {
            fiftyFiftyBreakdown(tokens[0], tokens[1], poolingToken.address)
        }

        return create(
            identifier = it,
            positionFetcher = defaultPositionFetcher(poolingToken.address),
            address = it,
            name = poolingToken.name,
            breakdown = breakdown,
            symbol = poolingToken.symbol,
            tokens = poolingToken.underlyingTokens,
            totalSupply = refreshable {
                getToken(it).totalDecimalSupply()
            },
            deprecated = false
        )
    }

    override fun getProtocol(): Protocol {
        return Protocol.ALIENBASE
    }

    override fun getNetwork(): Network {
        return Network.BASE
    }
}