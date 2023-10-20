package io.defitrack.protocol.equalizer

import arrow.core.Either
import arrow.fx.coroutines.parMap
import arrow.fx.coroutines.parMapNotNull
import io.defitrack.common.network.Network
import io.defitrack.common.utils.Refreshable
import io.defitrack.common.utils.Refreshable.Companion.refreshable
import io.defitrack.conditional.ConditionalOnCompany
import io.defitrack.market.pooling.PoolingMarketProvider
import io.defitrack.market.pooling.domain.PoolingMarket
import io.defitrack.protocol.Company
import io.defitrack.protocol.Protocol
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import org.springframework.stereotype.Component
import kotlin.coroutines.EmptyCoroutineContext

@ConditionalOnCompany(Company.EQUALIZER)
@Component
class EqualizerPoolingMarketProvider : PoolingMarketProvider() {

    private val voterAddress = "0x46abb88ae1f2a35ea559925d99fdc5441b592687"

    override suspend fun produceMarkets(): Flow<PoolingMarket> = channelFlow {
        val contract = EqualizerVoter(
            getBlockchainGateway(), voterAddress
        )

        contract.pools().parMapNotNull(concurrency = 12) { pool ->
            Either.catch {
                val lp = getToken(pool)

                if (lp.underlyingTokens.isEmpty()) {
                    throw IllegalArgumentException("Underlying tokens are empty for $pool")
                }

                create(
                    name = lp.name,
                    identifier = lp.address,
                    address = lp.address,
                    positionFetcher = defaultPositionFetcher(lp.address),
                    breakdown = fiftyFiftyBreakdown(
                        lp.underlyingTokens[0],
                        lp.underlyingTokens[1],
                        lp.address
                    ),
                    erc20Compatible = true,
                    deprecated = false,
                    symbol = lp.symbol,
                    tokens = lp.underlyingTokens,
                    totalSupply = refreshable(lp.totalDecimalSupply()) {
                        getToken(lp.address).totalDecimalSupply()
                    }
                )
            }.mapLeft {
                logger.error("Error while creating pooling market {}: {} ", pool, it.message)
            }.getOrNull()
        }.forEach {
            send(it)
        }
    }

    override fun getProtocol(): Protocol {
        return Protocol.EQUALIZER
    }

    override fun getNetwork(): Network {
        return Network.BASE
    }
}