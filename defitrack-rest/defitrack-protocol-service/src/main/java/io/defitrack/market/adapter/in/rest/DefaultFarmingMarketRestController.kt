package io.defitrack.market.adapter.`in`.rest

import io.defitrack.common.network.Network
import io.defitrack.invest.PrepareInvestmentCommand
import io.defitrack.market.adapter.`in`.mapper.FarmingMarketVOMapper
import io.defitrack.market.adapter.`in`.resource.FarmingMarketVO
import io.defitrack.market.adapter.`in`.resource.TransactionPreparationVO
import io.defitrack.market.domain.farming.FarmingMarket
import io.defitrack.market.domain.position.ExitPositionCommand
import io.defitrack.market.port.`in`.FarmingMarkets
import kotlinx.coroutines.runBlocking
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/{protocol}/staking", "/{protocol}/farming")
class DefaultFarmingMarketRestController(
    private val markets: FarmingMarkets,
    private val farmingMarketVOMapper: FarmingMarketVOMapper
) : DefaultMarketRestController<FarmingMarket>(
    markets, farmingMarketVOMapper
) {

    @GetMapping(value = ["/markets"], params = ["token", "network"])
    fun searchByToken(
        @PathVariable("protocol") protocol: String,
        @RequestParam("token") tokenAddress: String,
        @RequestParam("network") network: Network
    ): List<FarmingMarketVO> = runBlocking {
        markets.searchByToken(protocol, tokenAddress, network).map {
            farmingMarketVOMapper.map(it)
        }
    }

    @GetMapping(value = ["/markets/{marketId}"])
    suspend fun getById(
        @PathVariable("marketId") id: String,
    ): ResponseEntity<FarmingMarketVO> {
        return markets.getStakingMarketById(id)?.let {
            ResponseEntity.ok(
                farmingMarketVOMapper.map(it)
            )
        } ?: ResponseEntity.notFound().build()
    }

    @PostMapping(value = ["/markets/{id}/enter"])
    suspend fun prepareInvestment(
        @PathVariable("id") id: String, @RequestBody prepareInvestmentCommand: PrepareInvestmentCommand
    ): ResponseEntity<TransactionPreparationVO> {
        return markets.getStakingMarketById(id)?.investmentPreparer?.prepare(prepareInvestmentCommand)
            ?.let { transactions ->
                ResponseEntity.ok(
                    TransactionPreparationVO(
                        transactions
                    )
                )
            } ?: ResponseEntity.badRequest().build()
    }

    @PostMapping(value = ["/markets/{id}/exit"])
    suspend fun prepareExit(
        @PathVariable("id") id: String, @RequestBody exitPositionCommand: ExitPositionCommand
    ): ResponseEntity<TransactionPreparationVO> {
        return markets.getStakingMarketById(id)?.exitPositionPreparer?.prepare(exitPositionCommand)
            ?.let { transactions ->
                ResponseEntity.ok(
                    TransactionPreparationVO(
                        transactions
                    )
                )
            } ?: ResponseEntity.badRequest().build()
    }
}