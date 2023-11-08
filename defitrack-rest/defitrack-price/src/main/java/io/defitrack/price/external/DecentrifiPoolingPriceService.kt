package io.defitrack.price.external

import io.defitrack.erc20.TokenInformationVO
import io.defitrack.price.decentrifi.DecentrifiPoolingPriceRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.math.BigDecimal

@Component
class DecentrifiPoolingPriceService(
    private val decentrifiPoolingPriceRepository: DecentrifiPoolingPriceRepository
) : ExternalPriceService {

    private val logger = LoggerFactory.getLogger(this::class.java)

    override suspend fun appliesTo(token: TokenInformationVO): Boolean {
        return decentrifiPoolingPriceRepository.contains(token)
    }

    override suspend fun getAllPrices(): List<ExternalPrice> {
        return decentrifiPoolingPriceRepository.cache.asMap().entries.map {
            it.value
        }
    }

    override suspend fun getPrice(tokenInformationVO: TokenInformationVO): BigDecimal {
        return decentrifiPoolingPriceRepository.getPrice(tokenInformationVO).also {
            logger.info("getting price on decentrifi pooling for token ${tokenInformationVO.name} (${tokenInformationVO.symbol})")
        }
    }
}