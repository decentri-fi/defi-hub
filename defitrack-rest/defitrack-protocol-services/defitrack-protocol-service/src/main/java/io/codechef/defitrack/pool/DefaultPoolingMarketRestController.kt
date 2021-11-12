package io.codechef.defitrack.pool

import io.codechef.defitrack.logo.LogoService
import io.codechef.defitrack.network.toVO
import io.codechef.defitrack.pool.domain.PoolingMarketElement
import io.codechef.defitrack.pool.vo.PoolingMarketElementToken
import io.codechef.defitrack.pool.vo.PoolingMarketElementVO
import io.codechef.defitrack.protocol.toVO
import io.codechef.defitrack.token.TokenService
import io.defitrack.common.network.Network
import io.defitrack.protocol.staking.LpToken
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/pooling")
class DefaultPoolingMarketRestController(
    private val poolingMarketServices: List<PoolingMarketService>,
    private val logoService: LogoService,
    private val tokenService: TokenService,
) {

    companion object {
        val logger: Logger = LoggerFactory.getLogger(this::class.java)
    }

    @GetMapping(value = ["/markets"], params = ["token", "network"])
    fun searchByToken(
        @RequestParam("token") tokenAddress: String,
        @RequestParam("network") network: Network
    ): List<PoolingMarketElementVO> {
        return poolingMarketServices
            .filter {
                it.getNetwork() == network
            }
            .flatMap {
                it.getPoolingMarkets()
            }.filter {
                it.token.any { t ->
                    t.address.lowercase() == tokenAddress.lowercase()
                } || it.address.lowercase() == tokenAddress.lowercase()
            }.map { poolingMarketElementVO(it) }
    }

    @GetMapping(value = ["/markets/{id}"], params = ["network"])
    fun getById(
        @PathVariable("id") id: String,
        @RequestParam("network") network: Network
    ): ResponseEntity<PoolingMarketElementVO> {
        return poolingMarketServices
            .filter {
                it.getNetwork() == network
            }.flatMap {
                it.getPoolingMarkets()
            }.firstOrNull {
                it.id == id
            }?.let {
                ResponseEntity.ok(poolingMarketElementVO(it))
            } ?: ResponseEntity.notFound().build()
    }

    @GetMapping(value = ["/markets/alternatives"], params = ["token", "network"])
    fun findAlternatives(
        @RequestParam("token") tokenAddress: String,
        @RequestParam("network") network: Network
    ): List<PoolingMarketElementVO> {
        val token = tokenService.getTokenInformation(
            tokenAddress, network
        )
        return poolingMarketServices
            .filter {
                it.getNetwork() == network
            }
            .flatMap {
                it.getPoolingMarkets()
            }.filter { poolingMarketElement ->
                when (token) {
                    is LpToken -> {
                        poolingMarketElement.token.map { pt ->
                            pt.address.lowercase()
                        }.containsAll(listOf(token.token0.address.lowercase(), token.token1.address.lowercase()))
                    }
                    else -> false
                }
            }.map { poolingMarketElementVO(it) }
    }

    private fun poolingMarketElementVO(it: PoolingMarketElement) =
        PoolingMarketElementVO(
            name = it.name,
            protocol = it.protocol.toVO(),
            network = it.network.toVO(),
            token = it.token.map { token ->
                PoolingMarketElementToken(
                    name = token.name,
                    symbol = token.symbol,
                    address = token.address,
                    logo = logoService.generateLogoUrl(it.network, token.address),
                )
            },
            id = it.id,
            address = it.address,
            apr = it.apr,
            marketSize = it.marketSize
        )
}