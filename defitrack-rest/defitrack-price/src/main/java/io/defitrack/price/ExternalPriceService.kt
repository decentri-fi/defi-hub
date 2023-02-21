package io.defitrack.price

import io.defitrack.common.network.Network
import io.defitrack.erc20.TokenInformationVO
import java.math.BigDecimal

interface ExternalPriceService {
    fun getOracleName(): String = "unknown oracle"
    suspend fun getPrice(network: Network, tokenInformationVO: TokenInformationVO): BigDecimal

    fun appliesTo(network: Network, tokenInformationVO: TokenInformationVO): Boolean {
        return getOracleName() == tokenInformationVO.name.lowercase()
    }
}