package io.defitrack.price.external

import io.defitrack.common.network.Network
import io.defitrack.erc20.TokenInformationVO
import io.defitrack.price.decentrifi.DecentrifiUniswapV3UnderlyingPriceRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.math.BigDecimal

@Component
class DecentrifiUniswapV3PriceService(
    private val repository: DecentrifiUniswapV3UnderlyingPriceRepository
) : ExternalPriceService {

    private val logger = LoggerFactory.getLogger(this::class.java)

    override fun appliesTo(token: TokenInformationVO): Boolean {
        return repository.contains(token)
    }

    override fun getAllPrices(): List<ExternalPrice> {
        return repository.prices.asMap().entries.map {
            ExternalPrice(it.key.toString(), Network.ETHEREUM, it.value, "uniswap-v3")
        }
    }

    override suspend fun getPrice(tokenInformationVO: TokenInformationVO): BigDecimal {
        return repository.getPrice(tokenInformationVO).also {
            logger.info("getting logging price on decentrifi uniswapv3")
        } ?: BigDecimal.ZERO
    }
}