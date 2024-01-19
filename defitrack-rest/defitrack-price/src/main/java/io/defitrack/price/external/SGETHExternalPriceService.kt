package io.defitrack.price.external

import io.defitrack.common.network.Network
import io.defitrack.erc20.domain.FungibleTokenInformation
import io.defitrack.erc20.port.`in`.ERC20Resource
import io.defitrack.price.PriceProvider
import org.springframework.beans.factory.annotation.Autowired
import java.math.BigDecimal

//@Component
class SGETHExternalPriceService : ExternalPriceService {

    @Autowired
    private lateinit var priceprovider: PriceProvider

    @Autowired
    private lateinit var erC20Resource: ERC20Resource

    override suspend fun appliesTo(token: FungibleTokenInformation): Boolean {
        return token.network.name == Network.ETHEREUM.name &&
                token.address.lowercase() == "0x72e2f4830b9e45d52f80ac08cb2bec0fef72ed9c"
    }

    override suspend fun getAllPrices(): List<ExternalPrice> {
        return emptyList()
    }

    override suspend fun getPrice(fungibleToken: FungibleTokenInformation): BigDecimal {
        return priceprovider.getPrice(
            erC20Resource.getTokenInformation(Network.ETHEREUM, "0x0")
        )
    }
}