package io.defitrack.market.pooling

import io.defitrack.common.utils.FormatUtilsExtensions.asEth
import io.defitrack.market.pooling.breakdown.PoolingBreakdownMapper
import io.defitrack.market.pooling.domain.PoolingMarket
import io.defitrack.market.pooling.domain.PoolingPosition
import io.defitrack.market.pooling.vo.PoolingMarketVO
import io.defitrack.market.pooling.vo.PoolingPositionVO
import io.defitrack.network.toVO
import io.defitrack.price.PriceRequest
import io.defitrack.price.PriceResource
import io.defitrack.protocol.toVO
import io.defitrack.token.ERC20Resource
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/pooling")
class DefaultPoolingPositionRestController(
    private val poolingPositionProviders: List<PoolingPositionProvider>,
    private val priceResource: PriceResource,
    private val erC20Resource: ERC20Resource,
    private val breakdownService: PoolingBreakdownMapper,
    private val poolingBreakdownMapper: PoolingBreakdownMapper
) {

    val logger: Logger = LoggerFactory.getLogger(this::class.java)

    @GetMapping("/{userId}/positions")
    fun getUserPoolings(@PathVariable("userId") address: String): List<PoolingPositionVO> = runBlocking {
        poolingPositionProviders.map {
            async {
                try {
                    it.userPoolings(address)
                } catch (ex: Exception) {
                    emptyList()
                }
            }
        }.awaitAll().flatMap {
            it.map { poolingElement ->
                poolingElement.toVO()
            }
        }
    }

    suspend fun PoolingPosition.toVO(): PoolingPositionVO {
        val lpToken = erC20Resource.getTokenInformation(market.network, market.address)
        return PoolingPositionVO(
            lpAddress = market.address,
            amountDecimal = tokenAmount.asEth(lpToken.decimals),
            name = market.name,
            dollarValue = priceResource.calculatePrice(
                PriceRequest(
                    market.address,
                    market.network,
                    tokenAmount.asEth(lpToken.decimals),
                    lpToken.type
                )
            ),
            network = market.network.toVO(),
            symbol = market.symbol,
            protocol = market.protocol.toVO(),
            id = market.id,
            exitPositionSupported = market.exitPositionPreparer != null,
            amount = tokenAmount,
            breakdown = breakdownService.toPositionVO(market, tokenAmount.asEth(lpToken.decimals)),
            market = market.toVO()
        )
    }

    fun PoolingMarket.toVO(): PoolingMarketVO {
        return PoolingMarketVO(
            name = name,
            protocol = protocol.toVO(),
            network = network.toVO(),
            tokens = tokens,
            id = id,
            breakdown = poolingBreakdownMapper.toVO(breakdown),
            decimals = decimals,
            address = address,
            apr = apr,
            marketSize = marketSize,
            prepareInvestmentSupported = investmentPreparer != null,
            erc20Compatible = erc20Compatible,
            exitPositionSupported = exitPositionPreparer != null,
            price = price
        )
    }
}