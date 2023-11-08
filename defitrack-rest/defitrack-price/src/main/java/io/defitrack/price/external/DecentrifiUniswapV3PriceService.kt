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

    override suspend fun appliesTo(token: TokenInformationVO): Boolean {
        return repository.contains(token)
    }

    override suspend fun getAllPrices(): List<ExternalPrice> {
        return repository.prices.asMap().entries.map {
            it.value
        }
    }

    override suspend fun getPrice(tokenInformationVO: TokenInformationVO): BigDecimal {
        return repository.getPrice(tokenInformationVO)?.also {
            logger.info("getting price on decentrifi uniswapv3 for ${tokenInformationVO.name} (${tokenInformationVO.symbol}) on ${tokenInformationVO.network.name}")
        } ?: BigDecimal.ZERO
    }
}