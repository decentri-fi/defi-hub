package io.defitrack.protocol.idex

import io.defitrack.common.network.Network
import io.defitrack.pool.PoolingMarketService
import io.defitrack.pool.domain.PoolingMarketElement
import io.defitrack.protocol.Protocol
import io.defitrack.token.ERC20Resource
import io.defitrack.token.TokenType
import org.springframework.stereotype.Component
import java.math.BigDecimal

@Component
class IdexPoolingMarketService(
    private val idexService: IdexService,
    private val erc20Resource: ERC20Resource
) : PoolingMarketService() {

    override suspend fun fetchPoolingMarkets() = idexService.getLPs().mapNotNull {

        if (it.reserveUsd > BigDecimal.valueOf(10000)) {
            try {
                val token = erc20Resource.getTokenInformation(getNetwork(), it.liquidityToken)
                val token0 = erc20Resource.getTokenInformation(getNetwork(), it.tokenA)
                val token1 = erc20Resource.getTokenInformation(getNetwork(), it.tokenB)

                PoolingMarketElement(
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
                    tokenType = TokenType.IDEX
                )
            } catch (ex: Exception) {
                logger.error("something went wrong while importing ${it.liquidityToken}", ex)
                null
            }
        } else {
            null
        }
    }

    override fun getProtocol(): Protocol {
        return Protocol.IDEX
    }

    override fun getNetwork(): Network {
        return Network.POLYGON
    }
}