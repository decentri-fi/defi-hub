package io.defitrack.market.pooling

import io.defitrack.common.network.Network
import io.defitrack.farming.vo.TransactionPreparationVO
import io.defitrack.invest.PrepareInvestmentCommand
import io.defitrack.market.pooling.vo.PoolingMarketVO
import io.defitrack.market.pooling.vo.PoolingMarketVO.Companion.toVO
import io.defitrack.token.ERC20Resource
import io.defitrack.token.TokenType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/pooling")
class DefaultPoolingMarketRestController(
    private val poolingMarketProviders: List<PoolingMarketProvider>,
    private val erC20Resource: ERC20Resource
) {
    companion object {
        val logger: Logger = LoggerFactory.getLogger(this::class.java)
    }

    @GetMapping(value = ["/all-markets"])
    fun allMarkets(): List<PoolingMarketVO> {
        return poolingMarketProviders.flatMap {
            it.getPoolingMarkets()
        }.map {
            it.toVO()
        }
    }


    @PostMapping(value = ["/markets/{id}/invest"])
    fun prepareInvestment(
        @PathVariable("id") id: String,
        @RequestBody prepareInvestmentCommand: PrepareInvestmentCommand
    ): ResponseEntity<TransactionPreparationVO> = runBlocking {
        poolingMarketById(id)?.investmentPreparer?.prepare(prepareInvestmentCommand)?.let { transactions ->
            ResponseEntity.ok(
                TransactionPreparationVO(
                    transactions
                )
            )
        } ?: ResponseEntity.badRequest().build()
    }

    @GetMapping(value = ["/markets"], params = ["token", "network"])
    fun searchByToken(
        @RequestParam("token") tokenAddress: String,
        @RequestParam("network") network: Network
    ): List<PoolingMarketVO> {
        return poolingMarketProviders
            .filter {
                it.getNetwork() == network
            }
            .flatMap {
                it.getPoolingMarkets()
            }.filter {
                it.tokens.any { t ->
                    t.address.lowercase() == tokenAddress.lowercase()
                } || it.address.lowercase() == tokenAddress.lowercase()
            }.map { it.toVO() }
    }

    @GetMapping(value = ["/markets/{id}"])
    fun getById(
        @PathVariable("id") id: String,
    ): ResponseEntity<PoolingMarketVO> {
        return poolingMarketById(id)?.let {
            ResponseEntity.ok(it.toVO())
        } ?: ResponseEntity.notFound().build()
    }

    private fun poolingMarketById(
        id: String
    ) = poolingMarketProviders.flatMap {
        it.getPoolingMarkets()
    }.firstOrNull {
        it.id == id
    }

    @GetMapping(value = ["/markets/alternatives"], params = ["token", "network"])
    fun findAlternatives(
        @RequestParam("token") tokenAddress: String,
        @RequestParam("network") network: Network
    ): List<PoolingMarketVO> = runBlocking(Dispatchers.IO){
        val token = erC20Resource.getTokenInformation(
            network, tokenAddress,
        )
        poolingMarketProviders
            .filter {
                it.getNetwork() == network
            }
            .flatMap {
                it.getPoolingMarkets()
            }.filter { poolingMarketElement ->
                when {
                    (token.type) != TokenType.SINGLE -> {
                        poolingMarketElement.tokens.map { pt ->
                            pt.address.lowercase()
                        }.containsAll(
                            token.underlyingTokens.map {
                                it.address.lowercase()
                            }
                        )
                    }
                    else -> false
                }
            }.map { it.toVO() }
    }
}