package io.defitrack.market.pooling

import io.defitrack.common.utils.FormatUtilsExtensions.asEth
import io.defitrack.market.pooling.breakdown.PoolingBreakdownMapper
import io.defitrack.market.pooling.domain.PoolingPosition
import io.defitrack.market.pooling.mapper.PoolingMarketVOMapper
import io.defitrack.market.pooling.vo.PoolingPositionVO
import io.defitrack.network.toVO
import io.defitrack.protocol.mapper.ProtocolVOMapper
import io.defitrack.token.DecentrifiERC20Resource
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
    private val erC20Resource: DecentrifiERC20Resource,
    private val breakdownService: PoolingBreakdownMapper,
    private val protocolVOMapper: ProtocolVOMapper,
    private val poolingMarketVOMapper: PoolingMarketVOMapper
) {

    val logger: Logger = LoggerFactory.getLogger(this::class.java)

    @GetMapping("/{userId}/positions")
    fun getUserPoolings(@PathVariable("userId") address: String): List<PoolingPositionVO> = runBlocking {
        poolingPositionProviders.map {
            async {
                try {
                    it.userPoolings(address)
                } catch (ex: Exception) {
                    ex.printStackTrace()
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
        val amount = tokenAmount.asEth(lpToken.decimals)
        val dollarValue = customPriceCalculator?.calculate() ?: amount.times(market.price).toDouble()

        return PoolingPositionVO(
            lpAddress = market.address,
            amountDecimal = amount,
            name = market.name,
            dollarValue = dollarValue,
            network = market.network.toVO(),
            symbol = market.symbol,
            protocol = protocolVOMapper.map(market.protocol),
            id = market.id,
            exitPositionSupported = market.exitPositionPreparer != null,
            amount = tokenAmount,
            breakdown = breakdownService.toPositionVO(market, amount),
            market = poolingMarketVOMapper.map(market)
        )
    }


}