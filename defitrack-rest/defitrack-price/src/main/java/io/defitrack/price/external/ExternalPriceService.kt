package io.defitrack.price.external

import io.defitrack.erc20.TokenInformationVO
import java.math.BigDecimal

interface ExternalPriceService {
    fun getOracleName(): String = "unknown oracle"
    suspend fun getPrice(tokenInformationVO: TokenInformationVO): BigDecimal

    fun appliesTo(token: TokenInformationVO): Boolean {
        return getOracleName() == token.name.lowercase()
    }

    fun getAllPrices(): List<ExternalPrice>
}