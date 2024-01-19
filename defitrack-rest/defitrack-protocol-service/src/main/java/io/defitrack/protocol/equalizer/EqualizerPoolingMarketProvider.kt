package io.defitrack.protocol.equalizer

import arrow.core.Either
import arrow.fx.coroutines.parMapNotNull
import io.defitrack.common.network.Network
import io.defitrack.common.utils.refreshable
import io.defitrack.architecture.conditional.ConditionalOnCompany
import io.defitrack.market.port.out.PoolingMarketProvider
import io.defitrack.market.domain.PoolingMarket
import io.defitrack.protocol.Company
import io.defitrack.protocol.Protocol
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import org.springframework.stereotype.Component

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

                val breakdown = refreshable {
                    fiftyFiftyBreakdown(
                        lp.underlyingTokens[0],
                        lp.underlyingTokens[1],
                        lp.address
                    )
                }

                create(
                    name = lp.name,
                    identifier = lp.address,
                    address = lp.address,
                    positionFetcher = defaultPositionFetcher(lp.address),
                    breakdown = breakdown,
                    erc20Compatible = true,
                    symbol = lp.symbol,
                    tokens = lp.underlyingTokens,
                    totalSupply = refreshable {
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