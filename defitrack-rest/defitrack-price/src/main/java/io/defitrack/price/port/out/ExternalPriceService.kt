package io.defitrack.price.port.out

import io.defitrack.erc20.domain.FungibleTokenInformation
import io.defitrack.price.external.domain.ExternalPrice
import kotlinx.coroutines.flow.Flow

interface ExternalPriceService {

    fun order() = 50

    fun getOracleName(): String = "unknown oracle"
    suspend fun appliesTo(token: FungibleTokenInformation): Boolean {
        return getOracleName() == token.name.lowercase()
    }

    suspend fun getAllPrices(): Flow<ExternalPrice>
}