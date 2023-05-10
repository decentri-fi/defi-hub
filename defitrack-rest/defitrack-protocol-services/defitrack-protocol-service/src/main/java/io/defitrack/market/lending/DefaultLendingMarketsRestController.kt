package io.defitrack.market.lending

import io.defitrack.common.network.Network
import io.defitrack.invest.PrepareInvestmentCommand
import io.defitrack.market.farming.vo.TransactionPreparationVO
import io.defitrack.market.lending.mapper.LendingMarketVOMapper
import io.defitrack.market.lending.vo.LendingMarketVO
import io.ktor.http.*
import kotlinx.coroutines.runBlocking
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/lending")
class DefaultLendingMarketsRestController(
    private val lendingMarketProviders: List<LendingMarketProvider>,
    private val lendingMarketVOMapper: LendingMarketVOMapper
) {

    @GetMapping(value = ["/all-markets"])
    fun getAllMarkets(): List<LendingMarketVO> {
        return lendingMarketProviders.flatMap {
            it.getMarkets()
        }.map(lendingMarketVOMapper::map)
    }

    @GetMapping(value = ["/markets"], params = ["token"])
    fun searchByToken(
        @RequestParam("token") token: String,
        @RequestParam("network") network: Network
    ): List<LendingMarketVO> {
        return lendingMarketProviders
            .filter {
                it.getNetwork() == network
            }.flatMap {
                it.getMarkets()
            }.filter {
                it.token.address.lowercase() == token
            }.map(lendingMarketVOMapper::map)
    }


    private fun getLendingMarketById(
        id: String
    ) = lendingMarketProviders.flatMap {
        it.getMarkets()
    }.firstOrNull {
        it.id == id
    }

    @PostMapping(value = ["/markets/{id}/enter"])
    fun prepareInvestment(
        @PathVariable("id") id: String,
        @RequestBody prepareInvestmentCommand: PrepareInvestmentCommand
    ): ResponseEntity<TransactionPreparationVO> = runBlocking {
        getLendingMarketById(
            id
        )?.investmentPreparer?.prepare(prepareInvestmentCommand)?.let { transactions ->
            ResponseEntity.ok(
                TransactionPreparationVO(
                    transactions
                )
            )
        } ?: ResponseEntity.badRequest().build()
    }
}