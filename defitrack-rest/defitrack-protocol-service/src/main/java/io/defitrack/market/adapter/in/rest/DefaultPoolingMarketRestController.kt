package io.defitrack.market.adapter.`in`.rest

import io.defitrack.common.network.Network
import io.defitrack.invest.PrepareInvestmentCommand
import io.defitrack.market.adapter.`in`.resource.TransactionPreparationVO
import io.defitrack.market.domain.PoolingMarket
import io.defitrack.market.pooling.mapper.PoolingMarketVOMapper
import io.defitrack.market.pooling.vo.PoolingMarketVO
import io.defitrack.market.port.`in`.PoolingMarkets
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*


@RestController
@RequestMapping("/{protocol}/pooling")
class DefaultPoolingMarketRestController(
    private val poolingMarkets: PoolingMarkets,
    private val poolingMarketVOMapper: PoolingMarketVOMapper
) : DefaultMarketRestController<PoolingMarket>(
    poolingMarkets, poolingMarketVOMapper
) {
    @PostMapping(value = ["/markets/{id}/invest"])
    suspend fun prepareInvestment(
        @PathVariable("protocol") protocol: String,
        @PathVariable("id") id: String,
        @RequestBody prepareInvestmentCommand: PrepareInvestmentCommand
    ): ResponseEntity<TransactionPreparationVO> {
        return poolingMarketById(protocol, id)?.investmentPreparer?.prepare(prepareInvestmentCommand)
            ?.let { transactions ->
                ResponseEntity.ok(
                    TransactionPreparationVO(
                        transactions
                    )
                )
            } ?: ResponseEntity.badRequest().build()
    }

    @GetMapping(value = ["/markets"], params = ["token", "network"])
    suspend fun searchByToken(
        @PathVariable("protocol") protocol: String,
        @RequestParam("token") tokenAddress: String,
        @RequestParam("network") network: Network
    ): List<PoolingMarketVO> {
        return poolingMarkets.searchByToken(protocol, tokenAddress, network)
            .map {
                poolingMarketVOMapper.map(it)
            }
    }

    @GetMapping(value = ["/markets/{id}"])
    suspend fun getById(
        @PathVariable("protocol") protocol: String,
        @PathVariable("id") id: String,
        @RequestParam("refresh", required = false, defaultValue = "false") refresh: Boolean
    ): ResponseEntity<PoolingMarketVO> {
        return poolingMarketById(protocol, id)?.also {
            if (refresh) it.refresh()
        }?.let {
            ResponseEntity.ok(poolingMarketVOMapper.map(it))
        } ?: ResponseEntity.notFound().build()
    }

    private fun poolingMarketById(
        protocol: String,
        id: String
    ) = poolingMarkets.getAllMarkets(protocol).firstOrNull {
        it.id == id
    }

    @GetMapping(value = ["/markets/alternatives"], params = ["token", "network"])
    suspend fun findAlternatives(
        @PathVariable("protocol") protocol: String,
        @RequestParam("token") tokenAddress: String,
        @RequestParam("network") network: Network
    ): List<PoolingMarketVO> {
        return poolingMarkets.findAlternatives(protocol, tokenAddress, network)
            .map {
                poolingMarketVOMapper.map(it)
            }
    }
}