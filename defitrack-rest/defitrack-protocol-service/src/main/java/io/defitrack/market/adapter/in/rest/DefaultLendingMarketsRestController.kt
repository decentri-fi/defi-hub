package io.defitrack.market.adapter.`in`.rest

import io.defitrack.common.network.Network
import io.defitrack.invest.PrepareInvestmentCommand
import io.defitrack.market.adapter.`in`.resource.TransactionPreparationVO
import io.defitrack.market.domain.lending.LendingMarket
import io.defitrack.market.adapter.`in`.mapper.LendingMarketVOMapper
import io.defitrack.market.adapter.`in`.resource.LendingMarketVO
import io.defitrack.market.port.`in`.LendingMarkets
import io.defitrack.market.port.out.LendingMarketProvider
import io.swagger.v3.oas.annotations.tags.Tag
import kotlinx.coroutines.runBlocking
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/{protocol}/lending")
@Tag(name = "Lending Markets")
class DefaultLendingMarketsRestController(
    private val lendingMarkets: LendingMarkets,
    private val lendingMarketVOMapper: LendingMarketVOMapper
) : DefaultMarketRestController<LendingMarket>(
    lendingMarkets, lendingMarketVOMapper
) {

    @GetMapping(value = ["/markets"], params = ["token"])
    suspend fun searchByToken(
        @PathVariable("protocol") protocol: String,
        @RequestParam("token") token: String,
        @RequestParam("network") network: Network
    ): List<LendingMarketVO> {
        return lendingMarkets.searchByToken(protocol, token, network).map {
            lendingMarketVOMapper.map(it)
        }
    }


    private fun getLendingMarketById(
        protocol: String,
        id: String
    ) = lendingMarkets.getAllMarkets(protocol).firstOrNull {
        it.id == id
    }


    @PostMapping(value = ["/markets/{id}/enter"])
    fun prepareInvestment(
        @PathVariable("protocol") protocol: String,
        @PathVariable("id") id: String,
        @RequestBody prepareInvestmentCommand: PrepareInvestmentCommand
    ): ResponseEntity<TransactionPreparationVO> = runBlocking {
        getLendingMarketById(
            protocol, id
        )?.investmentPreparer?.prepare(prepareInvestmentCommand)?.let { transactions ->
            ResponseEntity.ok(
                TransactionPreparationVO(
                    transactions
                )
            )
        } ?: ResponseEntity.badRequest().build()
    }
}