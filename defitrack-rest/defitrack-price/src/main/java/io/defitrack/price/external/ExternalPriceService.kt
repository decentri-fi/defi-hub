package io.defitrack.price.external

import io.defitrack.domain.FungibleToken
import java.math.BigDecimal

interface ExternalPriceService {

    fun order() = 0

    fun getOracleName(): String = "unknown oracle"
    suspend fun getPrice(fungibleToken: FungibleToken): BigDecimal

    suspend fun appliesTo(token: FungibleToken): Boolean {
        return getOracleName() == token.name.lowercase()
    }

    suspend fun getAllPrices(): List<ExternalPrice>
}