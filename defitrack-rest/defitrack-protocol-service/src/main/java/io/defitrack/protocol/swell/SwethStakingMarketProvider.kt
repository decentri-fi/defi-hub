package io.defitrack.protocol.swell

import arrow.core.nel
import io.defitrack.common.network.Network
import io.defitrack.common.utils.AsyncUtils.lazyAsync
import io.defitrack.conditional.ConditionalOnCompany
import io.defitrack.market.farming.FarmingMarketProvider
import io.defitrack.market.farming.domain.FarmingMarket
import io.defitrack.protocol.Company
import io.defitrack.protocol.Protocol
import org.springframework.stereotype.Component

@Component
@ConditionalOnCompany(Company.SWELL)
class SwethStakingMarketProvider : FarmingMarketProvider() {

    val swethAddress = "0xf951e335afb289353dc249e82926178eac7ded78"

    val deferredContract = lazyAsync {
        SwethContract(getBlockchainGateway(), swethAddress)
    }

    override suspend fun fetchMarkets(): List<FarmingMarket> {
        val ether = getToken("0x0")
        val contract = deferredContract.await()

        return create(
            name = "swETH",
            identifier = swethAddress,
            stakedToken = ether,
            rewardToken = ether,
            positionFetcher = contract.positionFetcher()
        ).nel()
    }

    override fun getProtocol(): Protocol {
        return Protocol.SWELL
    }

    override fun getNetwork(): Network {
        return Network.ETHEREUM
    }
}