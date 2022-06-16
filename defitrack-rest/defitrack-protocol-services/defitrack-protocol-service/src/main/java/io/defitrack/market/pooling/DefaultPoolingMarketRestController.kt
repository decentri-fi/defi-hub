package io.defitrack.market.pooling

import io.defitrack.common.network.Network
import io.defitrack.market.pooling.vo.PoolingMarketVO
import io.defitrack.market.pooling.vo.PoolingMarketVO.Companion.toVO
import io.defitrack.token.ERC20Resource
import io.defitrack.token.TokenType
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

    @GetMapping(value = ["/markets/{id}"], params = ["network"])
    fun getById(
        @PathVariable("id") id: String,
        @RequestParam("network") network: Network
    ): ResponseEntity<PoolingMarketVO> {
        return poolingMarketProviders
            .filter {
                it.getNetwork() == network
            }.flatMap {
                it.getPoolingMarkets()
            }.firstOrNull {
                it.id == id
            }?.let {
                ResponseEntity.ok(it.toVO())
            } ?: ResponseEntity.notFound().build()
    }

    @GetMapping(value = ["/markets/alternatives"], params = ["token", "network"])
    fun findAlternatives(
        @RequestParam("token") tokenAddress: String,
        @RequestParam("network") network: Network
    ): List<PoolingMarketVO> {
        val token = erC20Resource.getTokenInformation(
            network, tokenAddress,
        )
        return poolingMarketProviders
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