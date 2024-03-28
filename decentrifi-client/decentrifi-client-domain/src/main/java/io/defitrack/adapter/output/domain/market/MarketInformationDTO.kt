package io.defitrack.adapter.output.domain.market

import io.defitrack.adapter.output.domain.meta.NetworkInformationDTO
import io.defitrack.adapter.output.domain.meta.ProtocolInformationDTO
import java.math.BigDecimal

abstract class MarketInformationDTO(
    val id: String,
    val network: NetworkInformationDTO,
    val protocol: ProtocolInformationDTO,
    val name: String,
    val prepareInvestmentSupported: Boolean,
    val exitPositionSupported: Boolean,
    val marketSize: BigDecimal?,
    val marketType: String,
    val updatedAt: Long,
    val deprecated: Boolean
)