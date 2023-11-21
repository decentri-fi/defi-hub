package io.defitrack.protocol.frax

import arrow.core.nel
import io.defitrack.common.network.Network
import io.defitrack.conditional.ConditionalOnCompany
import io.defitrack.market.farming.FarmingMarketProvider
import io.defitrack.market.farming.domain.FarmingMarket
import io.defitrack.protocol.Company
import io.defitrack.protocol.Protocol
import org.springframework.stereotype.Component

@Component
@ConditionalOnCompany(Company.FRAX)
class FraxStakedEthMarketProvider : FarmingMarketProvider() {

    val sfrxEthAddress = "0xac3e018457b222d93114458476f3e3416abbe38f"
    val fraxEtherAddress = "0x5e8422345238f34275888049021821e8e08caa1f"

    override suspend fun fetchMarkets(): List<FarmingMarket> {
        val fraxEther = getToken(fraxEtherAddress)
        return create(
            name = "Staked Frax Ether",
            identifier = sfrxEthAddress,
            stakedToken = fraxEther,
            rewardToken = fraxEther
        ).nel()
    }

    override fun getProtocol(): Protocol {
        return Protocol.FRAX
    }

    override fun getNetwork(): Network {
        return Network.ETHEREUM
    }
}