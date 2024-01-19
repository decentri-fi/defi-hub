package io.defitrack.price.external

import io.defitrack.erc20.domain.FungibleTokenInformation
import java.math.BigDecimal

interface ExternalPriceService {

    fun order() = 0

    fun getOracleName(): String = "unknown oracle"
    suspend fun getPrice(fungibleToken: FungibleTokenInformation): BigDecimal

    suspend fun appliesTo(token: FungibleTokenInformation): Boolean {
        return getOracleName() == token.name.lowercase()
    }

    suspend fun getAllPrices(): List<ExternalPrice>
}