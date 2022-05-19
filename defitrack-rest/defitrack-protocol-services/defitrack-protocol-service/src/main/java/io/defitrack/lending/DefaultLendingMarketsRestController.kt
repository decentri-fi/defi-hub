package io.defitrack.lending

import io.defitrack.common.network.Network
import io.defitrack.lending.domain.LendingMarket
import io.defitrack.lending.vo.LendingMarketElementToken
import io.defitrack.lending.vo.LendingMarketElementVO
import io.defitrack.network.toVO
import io.defitrack.protocol.toVO
import io.defitrack.invest.PrepareInvestmentCommand
import io.defitrack.staking.vo.TransactionPreparationVO
import kotlinx.coroutines.runBlocking
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/lending")
class DefaultLendingMarketsRestController(
    private val lendingMarketServices: List<LendingMarketService>,
) {

    @GetMapping(value = ["/all-markets"])
    fun getAllMarkets(): List<LendingMarketElementVO> {
        return lendingMarketServices.flatMap {
            it.getLendingMarkets()
        }.map {
            it.toVO()
        }
    }

    @GetMapping(value = ["/markets"], params = ["token"])
    fun searchByToken(
        @RequestParam("token") token: String,
        @RequestParam("network") network: Network
    ): List<LendingMarketElementVO> {
        return lendingMarketServices
            .filter {
                it.getNetwork() == network
            }.flatMap {
                it.getLendingMarkets()
            }.filter {
                it.token.address.lowercase() == token
            }.map { it.toVO() }
    }


    private fun getLendingMarketById(
        network: Network,
        id: String
    ) = lendingMarketServices
        .filter {
            it.getNetwork() == network
        }.flatMap {
            it.getLendingMarkets()
        }.firstOrNull {
            it.id == id
        }

    @PostMapping(value = ["/markets/{id}"], params = ["network"])
    fun prepareInvestment(
        @PathVariable("id") id: String,
        @RequestParam("network") network: Network,
        @RequestBody prepareInvestmentCommand: PrepareInvestmentCommand
    ): ResponseEntity<TransactionPreparationVO> = runBlocking {
        getLendingMarketById(
            network, id
        )?.investmentPreparer?.prepare(prepareInvestmentCommand)?.let { transactions ->
            ResponseEntity.ok(
                TransactionPreparationVO(
                    transactions
                )
            )
        } ?: ResponseEntity.badRequest().build()
    }

    fun LendingMarket.toVO(): LendingMarketElementVO {
        return LendingMarketElementVO(
            name = name,
            protocol = protocol.toVO(),
            network = network.toVO(),
            token = LendingMarketElementToken(
                name = token.name,
                symbol = token.symbol,
                address = token.address,
                logo = token.logo
            ),
            rate = rate?.toDouble(),
            poolType = poolType,
            marketSize = marketSize,
            prepareInvestmentSupported = investmentPreparer != null
        )
    }
}