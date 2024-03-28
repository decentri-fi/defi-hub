package io.defitrack.event

import io.defitrack.adapter.output.domain.erc20.FungibleTokenInformation
import io.defitrack.adapter.output.domain.meta.NetworkInformationDTO
import java.math.BigDecimal

data class LendingMarketUpdatedEvent(
    val id: String,
    val price: BigDecimal,
    val marketToken: FungibleTokenInformation?,
    val network: NetworkInformationDTO
) : MarketUpdatedEvent("lending")