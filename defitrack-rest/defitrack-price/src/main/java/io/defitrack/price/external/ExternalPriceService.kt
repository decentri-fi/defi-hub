package io.defitrack.price.external

import io.defitrack.erc20.TokenInformationVO
import java.math.BigDecimal

interface ExternalPriceService {

    fun order() = 0

    fun getOracleName(): String = "unknown oracle"
    suspend fun getPrice(tokenInformationVO: TokenInformationVO): BigDecimal

    suspend fun appliesTo(token: TokenInformationVO): Boolean {
        return getOracleName() == token.name.lowercase()
    }

    suspend fun getAllPrices(): List<ExternalPrice>
}