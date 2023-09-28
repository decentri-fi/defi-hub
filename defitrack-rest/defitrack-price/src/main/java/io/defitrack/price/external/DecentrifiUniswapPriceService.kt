package io.defitrack.price.external

import io.defitrack.common.network.Network
import io.defitrack.erc20.TokenInformationVO
import io.defitrack.price.decentrifi.DecentriUniswapV2UnderlyingPriceRepository
import org.springframework.stereotype.Component
import java.math.BigDecimal

@Component
class DecentrifiUniswapPriceService(
    private val decentriUniswapV2UnderlyingPriceRepository: DecentriUniswapV2UnderlyingPriceRepository
) : ExternalPriceService {

    override fun appliesTo(token: TokenInformationVO): Boolean {
        return token.network.toNetwork() == Network.ETHEREUM
                && decentriUniswapV2UnderlyingPriceRepository.contains(token.address)
    }

    override suspend fun getPrice(tokenInformationVO: TokenInformationVO): BigDecimal {
        return decentriUniswapV2UnderlyingPriceRepository.getPrice(tokenInformationVO.address) ?: BigDecimal.ZERO
    }
}