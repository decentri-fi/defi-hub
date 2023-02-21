package io.defitrack.market.pooling

import io.defitrack.common.network.Network
import io.defitrack.common.utils.BigDecimalExtensions.dividePrecisely
import io.defitrack.common.utils.BigDecimalExtensions.isZero
import io.defitrack.farming.vo.TransactionPreparationVO
import io.defitrack.invest.PrepareInvestmentCommand
import io.defitrack.market.pooling.domain.PoolingMarket
import io.defitrack.market.pooling.vo.PoolingMarketTokenShareVO
import io.defitrack.market.pooling.vo.PoolingMarketVO
import io.defitrack.network.toVO
import io.defitrack.price.PriceResource
import io.defitrack.protocol.toVO
import io.defitrack.token.ERC20Resource
import io.defitrack.token.TokenType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.math.BigDecimal

@RestController
@RequestMapping("/pooling")
class DefaultPoolingMarketRestController(
    private val poolingMarketProviders: List<PoolingMarketProvider>,
    private val erC20Resource: ERC20Resource,
    private val priceResource: PriceResource
) {
    companion object {
        val logger: Logger = LoggerFactory.getLogger(this::class.java)
    }

    @GetMapping(value = ["/all-markets"])
    fun allMarkets(@RequestParam(required = false, name = "network") network: Network?): List<PoolingMarketVO> =
        runBlocking {
            poolingMarketProviders
                .filter {
                    network?.let { network -> it.getNetwork() == network } ?: true
                }
                .map {
                    async {
                        it.getMarkets().map {
                            it.toVO()
                        }
                    }
                }.awaitAll().flatten()
        }


    @PostMapping(value = ["/markets/{id}/invest"])
    fun prepareInvestment(
        @PathVariable("id") id: String,
        @RequestBody prepareInvestmentCommand: PrepareInvestmentCommand
    ): ResponseEntity<TransactionPreparationVO> = runBlocking(Dispatchers.Default) {
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
                it.getMarkets()
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
        it.getMarkets()
    }.firstOrNull {
        it.id == id
    }

    @GetMapping(value = ["/markets/alternatives"], params = ["token", "network"])
    fun findAlternatives(
        @RequestParam("token") tokenAddress: String,
        @RequestParam("network") network: Network
    ): List<PoolingMarketVO> = runBlocking {
        val token = erC20Resource.getTokenInformation(
            network, tokenAddress,
        )
        poolingMarketProviders
            .filter {
                it.getNetwork() == network
            }
            .flatMap {
                it.getMarkets()
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

    fun PoolingMarket.toVO(): PoolingMarketVO {
        val totalReserveUSD = breakdown?.sumOf {
            it.reserveUSD
        } ?: BigDecimal.ZERO
        return PoolingMarketVO(
            name = name,
            protocol = protocol.toVO(),
            network = network.toVO(),
            tokens = tokens,
            id = id,
            breakdown = breakdown?.map {
                PoolingMarketTokenShareVO(
                    token = it.token,
                    reserve = it.reserve,
                    reserveUSD = it.reserveUSD,
                    weight = if (it.reserveUSD.isZero() || totalReserveUSD.isZero()) BigDecimal.ZERO else it.reserveUSD.dividePrecisely(
                        totalReserveUSD
                    )
                )
            },
            decimals = decimals,
            address = address,
            apr = apr,
            marketSize = marketSize,
            prepareInvestmentSupported = investmentPreparer != null,
            erc20Compatible = erc20Compatible,
        )
    }
}