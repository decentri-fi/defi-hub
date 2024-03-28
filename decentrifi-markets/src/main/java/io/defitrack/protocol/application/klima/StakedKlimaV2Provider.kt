package io.defitrack.protocol.application.klima

import arrow.core.nel
import io.defitrack.common.network.Network
import io.defitrack.architecture.conditional.ConditionalOnCompany
import io.defitrack.market.port.out.FarmingMarketProvider
import io.defitrack.market.domain.farming.FarmingMarket
import io.defitrack.protocol.Company
import io.defitrack.protocol.Protocol
import org.springframework.stereotype.Component

@Component
@ConditionalOnCompany(Company.KLIMA_DAO)
class StakedKlimaV2Provider : FarmingMarketProvider() {

    val klimaAddress = "0x4e78011ce80ee02d2c3e649fb657e45898257815"
    val sKlima = "0xb0c22d8d350c67420f06f48936654f567c73e8c8"

    //todo: unstake
    val stakingContract = "0x25d28a24ceb6f81015bb0b2007d795acac411b4d" //used for unstake

    override suspend fun fetchMarkets(): List<FarmingMarket> {
        val klima = getToken(klimaAddress)
        return create(
            name = "Staked KLIMA v2",
            identifier = "0xb0c22d8d350c67420f06f48936654f567c73e8c8",
            stakedToken = klima,
            rewardToken = klima,
            type = "klima.staking",
            positionFetcher = defaultPositionFetcher(sKlima)
        ).nel()
    }

    override fun getProtocol() = Protocol.KLIMA_DAO

    override fun getNetwork() = Network.POLYGON
}