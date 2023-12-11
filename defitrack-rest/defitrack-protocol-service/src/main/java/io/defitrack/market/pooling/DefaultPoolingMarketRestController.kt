package io.defitrack.market.pooling

import io.defitrack.common.network.Network
import io.defitrack.invest.PrepareInvestmentCommand
import io.defitrack.market.DefaultMarketRestController
import io.defitrack.market.farming.vo.TransactionPreparationVO
import io.defitrack.market.pooling.domain.PoolingMarket
import io.defitrack.market.pooling.mapper.PoolingMarketVOMapper
import io.defitrack.market.pooling.vo.PoolingMarketVO
import io.defitrack.token.DecentrifiERC20Resource
import io.defitrack.token.TokenType
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*


@RestController
@RequestMapping("/{protocol}/pooling")
class DefaultPoolingMarketRestController(
    private val poolingMarketProviders: List<PoolingMarketProvider>,
    private val erC20Resource: DecentrifiERC20Resource,
    private val poolingMarketVOMapper: PoolingMarketVOMapper
) : DefaultMarketRestController<PoolingMarket>(
    poolingMarketProviders, poolingMarketVOMapper
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
        return poolingMarketById(id)?.investmentPreparer?.prepare(prepareInvestmentCommand)?.let { transactions ->
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
        return poolingMarketProviders
            .filter {
                it.getProtocol().slug == protocol
            }
            .filter {
                it.getNetwork() == network
            }
            .flatMap {
                it.getMarkets()
            }.filter {
                it.tokens.any { t ->
                    t.address.lowercase() == tokenAddress.lowercase()
                } || it.address.lowercase() == tokenAddress.lowercase()
            }.map(poolingMarketVOMapper::map)
    }

    @GetMapping(value = ["/markets/{id}"])
    fun getById(
        @PathVariable("id") id: String,
    ): ResponseEntity<PoolingMarketVO> {
        return poolingMarketById(id)?.let {
            ResponseEntity.ok(poolingMarketVOMapper.map(it))
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
    suspend fun findAlternatives(
        @PathVariable("protocol") protocol: String,
        @RequestParam("token") tokenAddress: String,
        @RequestParam("network") network: Network
    ): List<PoolingMarketVO> {
        val token = erC20Resource.getTokenInformation(
            network, tokenAddress,
        )
        return poolingMarketProviders
            .filter {
                it.getProtocol().slug == protocol
            }
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
            }.map(poolingMarketVOMapper::map)
    }
}