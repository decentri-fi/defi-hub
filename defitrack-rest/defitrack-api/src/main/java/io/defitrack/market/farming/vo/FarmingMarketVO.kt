package io.defitrack.market.farming.vo

import io.defitrack.market.MarketVO
import io.defitrack.network.NetworkVO
import io.defitrack.protocol.ContractType
import io.defitrack.protocol.ProtocolVO
import io.defitrack.token.FungibleToken
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*

class FarmingMarketVO(
    id: String,
    network: NetworkVO,
    protocol: ProtocolVO,
    name: String,
    val stakedToken: FungibleToken,
    val reward: List<FungibleToken>,
    val vaultType: String,
    marketSize: BigDecimal?,
    val apr: BigDecimal?,
    prepareInvestmentSupported: Boolean,
    exitPositionSupported: Boolean,
    val farmType: ContractType,
    val expired: Boolean,
    updatedAt: Date
) : MarketVO(
    id, network, protocol, name, prepareInvestmentSupported, exitPositionSupported, marketSize, "farming", updatedAt.time
)
