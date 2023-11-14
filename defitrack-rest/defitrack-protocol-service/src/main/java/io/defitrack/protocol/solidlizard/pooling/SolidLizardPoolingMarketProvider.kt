package io.defitrack.protocol.solidlizard.pooling

import arrow.fx.coroutines.parMap
import arrow.fx.coroutines.parMapNotNull
import io.defitrack.common.network.Network
import io.defitrack.common.utils.FormatUtilsExtensions.asEth
import io.defitrack.common.utils.Refreshable.Companion.refreshable
import io.defitrack.conditional.ConditionalOnCompany
import io.defitrack.erc20.TokenInformationVO
import io.defitrack.market.pooling.PoolingMarketProvider
import io.defitrack.market.pooling.domain.PoolingMarket
import io.defitrack.protocol.Company
import io.defitrack.protocol.Protocol
import io.defitrack.uniswap.v2.PairFactoryContract
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.springframework.stereotype.Component

@Component
@ConditionalOnCompany(Company.SOLIDLIZARD)
class SolidLizardPoolingMarketProvider : PoolingMarketProvider() {
    override suspend fun fetchMarkets(): List<PoolingMarket> {
        val factory = PairFactoryContract(
            getBlockchainGateway(), "0x734d84631f00dc0d3fcd18b04b6cf42bfd407074"
        )

        return factory.allPairs().parMapNotNull(concurrency = 12) {

            val token = getToken(it)
            val tokens = token.underlyingTokens.map(TokenInformationVO::toFungibleToken)

            try {
                val breakdown = fiftyFiftyBreakdown(tokens[0], tokens[1], token.address)
                create(
                    name = token.name,
                    identifier = token.address,
                    marketSize = refreshable(breakdown.sumOf { it.reserveUSD }) {
                        fiftyFiftyBreakdown(tokens[0], tokens[1], token.address).sumOf { it.reserveUSD }
                    },
                    positionFetcher = defaultPositionFetcher(token.address),
                    tokens = token.underlyingTokens.map(TokenInformationVO::toFungibleToken),
                    symbol = token.symbol,
                    breakdown = breakdown,
                    address = token.address,
                    totalSupply = refreshable(token.totalSupply.asEth(token.decimals)) {
                        getToken(it).totalSupply.asEth(token.decimals)
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