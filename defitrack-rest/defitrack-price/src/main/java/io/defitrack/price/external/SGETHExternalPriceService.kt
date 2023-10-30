package io.defitrack.price.external

import io.defitrack.common.network.Network
import io.defitrack.erc20.TokenInformationVO
import io.defitrack.price.BeefyPricesService
import io.defitrack.price.PriceProvider
import io.defitrack.token.ERC20Resource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.stereotype.Service
import java.math.BigDecimal

//@Component
class SGETHExternalPriceService : ExternalPriceService {

    @Autowired
    private lateinit var priceprovider: PriceProvider

    @Autowired
    private lateinit var erC20Resource: ERC20Resource

    override fun appliesTo(token: TokenInformationVO): Boolean {
        return token.network.name == Network.ETHEREUM.name &&
                token.address.lowercase() == "0x72e2f4830b9e45d52f80ac08cb2bec0fef72ed9c"
    }

    override fun getAllPrices(): List<ExternalPrice> {
        return emptyList()
    }

    override suspend fun getPrice(tokenInformationVO: TokenInformationVO): BigDecimal {
        return priceprovider.getPrice(
            erC20Resource.getTokenInformation(Network.ETHEREUM, "0x0")
        )
    }
}