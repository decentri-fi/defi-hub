package io.defitrack.protocol.application.solidlizard

import arrow.fx.coroutines.parMapNotNull
import io.defitrack.common.network.Network
import io.defitrack.common.utils.refreshable
import io.defitrack.architecture.conditional.ConditionalOnCompany
import io.defitrack.market.port.out.PoolingMarketProvider
import io.defitrack.market.domain.PoolingMarket
import io.defitrack.protocol.Company
import io.defitrack.protocol.Protocol
import io.defitrack.uniswap.v2.PairFactoryContract
import org.springframework.stereotype.Component

@Component
@ConditionalOnCompany(Company.SOLIDLIZARD)
class SolidLizardPoolingMarketProvider : PoolingMarketProvider() {
    override suspend fun fetchMarkets(): List<PoolingMarket> {
        val factory = with(getBlockchainGateway()) {
            PairFactoryContract("0x734d84631f00dc0d3fcd18b04b6cf42bfd407074")
        }

        return factory
            .allPairs()
            .parMapNotNull(concurrency = 12) {

                val token = getToken(it)
                val tokens = token.underlyingTokens

                try {
                    val breakdown = refreshable {
                        fiftyFiftyBreakdown(tokens[0], tokens[1], token.address)
                    }
                    create(
                        name = token.name,
                        identifier = token.address,
                        positionFetcher = defaultPositionFetcher(token.address),
                        tokens = token.underlyingTokens,
                        symbol = token.symbol,
                        breakdown = breakdown,
                        address = token.address,
                        totalSupply = refreshable {
                            getToken(it).totalDecimalSupply()
                        }
                    )
                } catch (ex: Exception) {
                    logger.error("Error while fetching pooling market $it", ex)
                    null
                }
            }.filterNotNull()
    }

    override fun getProtocol(): Protocol {
        return Protocol.SOLIDLIZARD
    }

    override fun getNetwork(): Network {
        return Network.ARBITRUM
    }
}