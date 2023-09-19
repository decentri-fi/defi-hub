package io.defitrack.price.external

import io.defitrack.common.network.Network
import io.defitrack.erc20.TokenInformationVO
import io.defitrack.price.BeefyPricesService
import org.springframework.stereotype.Service
import java.math.BigDecimal

@Service
class SGETHExternalPriceService(
    private val beefyPricesService: BeefyPricesService
) : ExternalPriceService {

    override fun appliesTo(token: TokenInformationVO): Boolean {
        return token.network.name == Network.ETHEREUM.name &&
                token.address.lowercase() == "0x72e2f4830b9e45d52f80ac08cb2bec0fef72ed9c"
    }

    override suspend fun getPrice(tokenInformationVO: TokenInformationVO): BigDecimal {
        return beefyPricesService.getPrices()
            .getOrDefault("ETH", BigDecimal.ZERO)
    }
}