package io.defitrack.protocol.camelot.pooling

import arrow.fx.coroutines.parMapNotNull
import io.defitrack.common.network.Network
import io.defitrack.common.utils.FormatUtilsExtensions.asEth
import io.defitrack.common.utils.refreshable
import io.defitrack.architecture.conditional.ConditionalOnCompany
import io.defitrack.market.port.out.PoolingMarketProvider
import io.defitrack.market.domain.PoolingMarket
import io.defitrack.protocol.Company
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.algebra.AlgebraPoolContract
import io.defitrack.protocol.algebra.AlgebraPosition
import io.defitrack.protocol.camelot.CamelotService
import org.springframework.stereotype.Component
import java.math.BigDecimal


@Component
@ConditionalOnCompany(Company.CAMELOT)
class CamelotNFTV2PoolingMarketProvider(
    private val camelotService: CamelotService
) : PoolingMarketProvider() {


    override suspend fun fetchMarkets(): List<PoolingMarket> {
        val allPositions = camelotService.getAllPositions()
        val positions = allPositions.distinctBy {
            listOf(it.token0, it.token1).sorted().joinToString("-")
        }.map {
            it to AlgebraPoolContract(
                getBlockchainGateway(),
                camelotService.getPoolByPair(it.token0, it.token1)
            )
        }

        resolve(positions.map { it.second })

        return positions.parMapNotNull(concurrency = 8) {
            toMarket(it.first, it.second)
        }
    }

    override fun getProtocol(): Protocol {
        return Protocol.CAMELOT
    }

    suspend fun toMarket(it: AlgebraPosition, pool: AlgebraPoolContract): PoolingMarket {
        val token0 = getToken(it.token0)
        val token1 = getToken(it.token1)

        return create(
            tokens = listOf(token0, token1),
            identifier = pool.address,
            address = pool.address,
            name = "Camelot V3 ${token0.symbol}/${token1.symbol}",
            symbol = token0.symbol + "/" + token1.symbol,
            breakdown = refreshable(emptyList()),
            erc20Compatible = false,
            totalSupply = refreshable {
                pool.liquidity.await().asEth()
            },
            marketSize = refreshable {
                getMarketSize(
                    listOf(token0, token1),
                    pool.address
                )
            },
            price = refreshable(BigDecimal.ZERO)
        )
    }

    override fun getNetwork(): Network {
        return Network.ARBITRUM
    }
}