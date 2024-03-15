package io.defitrack.protocol.stargate.farming

import arrow.core.nel
import io.defitrack.common.network.Network
import io.defitrack.common.utils.refreshable
import io.defitrack.architecture.conditional.ConditionalOnCompany
import io.defitrack.market.port.out.FarmingMarketProvider
import io.defitrack.market.domain.farming.FarmingMarket
import io.defitrack.evm.position.PositionFetcher
import io.defitrack.protocol.Company
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.stargate.contract.VeSTGContract
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component

@Component
@ConditionalOnCompany(Company.STARGATE)
@ConditionalOnProperty(value = ["optimism.enabled"], havingValue = "true", matchIfMissing = true)
class StargateOptimismVestedFarmProvider : FarmingMarketProvider() {

    val veSTGAddress = "0x43d2761ed16c89a2c4342e2b16a3c61ccf88f05b"
    val stgAddress = "0x296f55f8fb28e498b858d0bcda06d955b2cb3f97"

    override suspend fun fetchMarkets(): List<FarmingMarket> {

        val contract = VeSTGContract(
            getBlockchainGateway(),
            veSTGAddress,
        )

        val stg = getToken(stgAddress)

        return create(
            name = "Vested STG",
            identifier = "stg-vested",
            stakedToken = stg,
            rewardToken = stg,
            marketSize = refreshable { getMarketSize(stg, veSTGAddress) },
            positionFetcher = PositionFetcher(contract::lockedFn),
            type = "stargate.vested-stg"
        ).nel()
    }

    override fun getProtocol(): Protocol {
        return Protocol.STARGATE
    }

    override fun getNetwork(): Network {
        return Network.OPTIMISM
    }
}