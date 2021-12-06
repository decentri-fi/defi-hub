package io.defitrack.protocol.idex

import io.defitrack.common.network.Network
import io.defitrack.pool.PoolingMarketService
import io.defitrack.pool.domain.PoolingMarketElement
import io.defitrack.pool.domain.PoolingToken
import io.defitrack.protocol.Protocol
import io.defitrack.token.TokenService
import io.github.reactivecircus.cache4k.Cache
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.util.concurrent.Executors
import javax.annotation.PostConstruct
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

@Component
class IdexPoolingMarketService(
    private val idexService: IdexService,
    private val tokenService: TokenService
) : PoolingMarketService {

    @OptIn(ExperimentalTime::class)
    private val cache = Cache.Builder().expireAfterWrite(
        Duration.Companion.hours(3)
    ).build<String, List<PoolingMarketElement>>()

    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java)
    }

    @PostConstruct
    fun intitialPopulation() {
        Executors.newSingleThreadExecutor().submit {
            getPoolingMarkets()
        }
    }

    override fun getPoolingMarkets(): List<PoolingMarketElement> {
        return runBlocking {
            cache.get("all") {
                fetchPoolingMarkets()
            }
        }
    }

    private fun fetchPoolingMarkets() = idexService.getLPs().mapNotNull {

        if (it.reserveUsd > BigDecimal.valueOf(10000)) {
            try {
                val token0 = tokenService.getTokenInformation(it.tokenA, getNetwork());
                val token1 = tokenService.getTokenInformation(it.tokenB, getNetwork())

                val element = PoolingMarketElement(
                    network = getNetwork(),
                    protocol = getProtocol(),
                    address = it.liquidityToken,
                    id = "idex-polygon-${it.liquidityToken}",
                    name = "IDEX ${token0.symbol}-${token1.symbol}",
                    token = listOf(
                        PoolingToken(
                            token0.name,
                            token0.symbol,
                            token0.address
                        ),
                        PoolingToken(
                            token1.name,
                            token1.symbol,
                            token1.address
                        ),
                    ),
                    apr = BigDecimal.ZERO,
                    marketSize = it.reserveUsd
                )
                logger.info("imported ${element.id}")
                element
            } catch (ex: Exception) {
                logger.error("something went wrong while importing ${it.liquidityToken}", ex)
                null
            }
        } else {
            null
        }
    }

    override fun getProtocol(): Protocol {
        return Protocol.IDEX
    }

    override fun getNetwork(): Network {
        return Network.POLYGON
    }
}