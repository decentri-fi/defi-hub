package io.defitrack.farming

import io.defitrack.common.network.Network
import io.defitrack.farming.vo.TransactionPreparationVO
import io.defitrack.invest.PrepareInvestmentCommand
import io.defitrack.market.farming.FarmingMarketService
import io.defitrack.market.farming.vo.FarmingMarketVO
import io.defitrack.market.farming.vo.FarmingMarketVO.Companion.toVO
import io.defitrack.token.ERC20Resource
import io.defitrack.token.TokenType
import kotlinx.coroutines.runBlocking
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/staking")
class DefaultFarmingMarketRestController(
    private val farmingMarketServices: List<FarmingMarketService>,
    private val erC20Resource: ERC20Resource
) {


    @GetMapping("/all-markets")
    fun stakingMarkets(
        @RequestParam("network") network: Network?
    ): List<FarmingMarketVO> {
        return farmingMarketServices
            .filter { marketService ->
                network?.let {
                    marketService.getNetwork() == it
                } ?: true
            }.flatMap {
                it.getStakingMarkets()
            }.map {
                it.toVO()
            }
    }

    @GetMapping(value = ["/markets"], params = ["token", "network"])
    fun searchByToken(
        @RequestParam("token") tokenAddress: String,
        @RequestParam("network") network: Network
    ): List<FarmingMarketVO> {
        return farmingMarketServices
            .filter {
                it.getNetwork() == network
            }.flatMap {
                it.getStakingMarkets()
            }.filter {
                val token = erC20Resource.getTokenInformation(it.network, it.stakedToken.address.lowercase())
                if (token.type != TokenType.SINGLE) {
                    it.stakedToken.address.lowercase() == tokenAddress.lowercase() ||
                            token.underlyingTokens.any { underlyingToken ->
                                underlyingToken.address.lowercase() == tokenAddress.lowercase()
                            }
                } else {
                    it.stakedToken.address.lowercase() == tokenAddress.lowercase()
                }
            }.map {
                it.toVO()
            }
    }

    @GetMapping(value = ["/markets/{marketId}"])
    fun getById(
        @PathVariable("marketId") id: String,
    ): ResponseEntity<FarmingMarketVO> {
        return getStakingMarketById(id)?.let {
            ResponseEntity.ok(it.toVO())
        } ?: ResponseEntity.notFound().build()
    }

    private fun getStakingMarketById(
        id: String
    ) = farmingMarketServices
        .flatMap {
            it.getStakingMarkets()
        }.firstOrNull {
            it.id == id
        }

    @PostMapping(value = ["/markets/{id}/invest"], params = ["network"])
    fun prepareInvestment(
        @PathVariable("id") id: String,
        @RequestParam("network") network: Network,
        @RequestBody prepareInvestmentCommand: PrepareInvestmentCommand
    ): ResponseEntity<TransactionPreparationVO> = runBlocking {
        getStakingMarketById(id)?.investmentPreparer?.prepare(prepareInvestmentCommand)?.let { transactions ->
            ResponseEntity.ok(
                TransactionPreparationVO(
                    transactions
                )
            )
        } ?: ResponseEntity.badRequest().build()
    }
}