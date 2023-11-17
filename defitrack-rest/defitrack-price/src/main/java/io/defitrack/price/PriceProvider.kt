package io.defitrack.price

import io.defitrack.erc20.TokenInformationVO
import io.defitrack.price.coingecko.CoinGeckoPriceService
import io.defitrack.price.external.ExternalPriceService
import io.github.reactivecircus.cache4k.Cache
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.math.BigDecimal
import kotlin.time.Duration.Companion.hours

@Component
class PriceProvider(
    private val externalPriceServices: List<ExternalPriceService>,
    private val coingeckoSerivce: CoinGeckoPriceService
) {

    val logger = LoggerFactory.getLogger(this::class.java)

    val priceCache = Cache.Builder<String, BigDecimal>().expireAfterWrite(1.hours).build()

    suspend fun getPrice(token: TokenInformationVO): BigDecimal {
        return priceCache.get(token.address.lowercase() + "-" + token.network.name) {
            externalPriceServices.find {
                it.appliesTo(token)
            }?.getPrice(token)
                ?: fromCoingecko(token)
                ?: BigDecimal.ZERO
        }
    }

    private suspend fun fromCoingecko(token: TokenInformationVO): BigDecimal? {
        return coingeckoSerivce.getPrice(token.address)?.also {
            logger.info("getting price on coingecko for ${token.name} (${token.symbol}) on ${token.network.name}")
        } ?: BigDecimal.ZERO
    }
}