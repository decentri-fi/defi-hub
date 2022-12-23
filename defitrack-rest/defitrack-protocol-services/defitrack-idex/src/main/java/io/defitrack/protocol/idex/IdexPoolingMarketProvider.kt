package io.defitrack.protocol.idex

import io.defitrack.common.network.Network
import io.defitrack.market.pooling.PoolingMarketProvider
import io.defitrack.market.pooling.domain.PoolingMarket
import io.defitrack.protocol.Protocol
import io.defitrack.token.ERC20Resource
import io.defitrack.token.TokenType
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.springframework.stereotype.Component
import java.math.BigDecimal

@Component
class IdexPoolingMarketProvider(
    private val idexService: IdexService,
    erc20Resource: ERC20Resource
) : PoolingMarketProvider(erc20Resource) {

    override suspend fun fetchMarkets() = coroutineScope {
        idexService.getLPs().map {
            async {
                try {
                    if (it.reserveUsd > BigDecimal.valueOf(10000)) {
                        try {
                            val token = erc20Resource.getTokenInformation(getNetwork(), it.liquidityToken)
                            val token0 = erc20Resource.getTokenInformation(getNetwork(), it.tokenA)
                            val token1 = erc20Resource.getTokenInformation(getNetwork(), it.tokenB)

                            PoolingMarket(
                                network = getNetwork(),
                                protocol = getProtocol(),
                                address = it.liquidityToken,
                                symbol = token.symbol,
                                id = "idex-polygon-${it.liquidityToken}",
                                name = "IDEX ${token0.symbol}-${token1.symbol}",
                                tokens = listOf(
                                    token0.toFungibleToken(),
                                    token1.toFungibleToken(),
                                ),
                                apr = BigDecimal.ZERO,
                                marketSize = it.reserveUsd,
                                tokenType = TokenType.IDEX,
                                balanceFetcher = defaultBalanceFetcher(token.address)
                            )
                        } catch (ex: Exception) {
                            logger.error("something went wrong while importing ${it.liquidityToken}", ex)
                            null
                        }
                    } else {
                        null
                    }
                } catch (ex: Exception) {
                    ex.printStackTrace()
                    null
                }
            }
        }.awaitAll().filterNotNull()
    }

    override fun getProtocol(): Protocol {
        return Protocol.IDEX
    }

    override fun getNetwork(): Network {
        return Network.POLYGON
    }
}