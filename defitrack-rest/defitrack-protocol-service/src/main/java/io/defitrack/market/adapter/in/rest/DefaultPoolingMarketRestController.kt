package io.defitrack.market.adapter.`in`.rest

import io.defitrack.common.network.Network
import io.defitrack.erc20.port.`in`.ERC20Resource
import io.defitrack.invest.PrepareInvestmentCommand
import io.defitrack.market.port.out.PoolingMarketProvider
import io.defitrack.market.adapter.`in`.resource.TransactionPreparationVO
import io.defitrack.market.domain.PoolingMarket
import io.defitrack.market.pooling.mapper.PoolingMarketVOMapper
import io.defitrack.market.pooling.vo.PoolingMarketVO
import io.defitrack.market.port.`in`.PoolingMarkets
import io.defitrack.token.TokenType
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*


@RestController
@RequestMapping("/{protocol}/pooling")
class DefaultPoolingMarketRestController(
    private val poolingMarkets: PoolingMarkets,
    private val erC20Resource: ERC20Resource,
    private val poolingMarketVOMapper: PoolingMarketVOMapper
) : DefaultMarketRestController<PoolingMarket>(
    poolingMarkets, poolingMarketVOMapper
) {
    companion object {
        val logger: Logger = LoggerFactory.getLogger(this::class.java)
    }

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
            .map(poolingMarketVOMapper::map)
    }

    @GetMapping(value = ["/markets/{id}"])
    fun getById(
        @PathVariable("protocol") protocol: String,
        @PathVariable("id") id: String,
    ): ResponseEntity<PoolingMarketVO> {
        return poolingMarketById(protocol, id)?.let {
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
            .map(poolingMarketVOMapper::map)
    }
}