package io.defitrack.lqty.farming

import arrow.core.nel
import io.defitrack.common.network.Network
import io.defitrack.conditional.ConditionalOnCompany
import io.defitrack.market.farming.FarmingMarketProvider
import io.defitrack.market.farming.domain.FarmingMarket
import io.defitrack.market.position.PositionFetcher
import io.defitrack.protocol.Company
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.liquity.LiquityStakingContract
import org.springframework.stereotype.Component

@ConditionalOnCompany(Company.LIQUITY)
@Component
class LiquityStakingMarketProvider : FarmingMarketProvider() {

    val stakingContractAddress = "0x4f9fbb3f1e99b56e0fe2892e623ed36a76fc605d"
    val lqtyAddress = "0x6dea81c8171d0ba574754ef6f8b412f2ed88c54d"
    val lusdAddress = "0x5f98805A4E8be255a32880FDeC7F6728C6568bA0"

    override suspend fun fetchMarkets(): List<FarmingMarket> {
        //TODO: 2 type of rewards eth and lusd
        val stakedToken = getToken(lqtyAddress)
        val rewardToken = getToken(lusdAddress)

        val stakingContract = LiquityStakingContract(getBlockchainGateway(), stakingContractAddress)

        return create(
            name = "LQTY Staking",
            identifier = stakingContractAddress,
            stakedToken = stakedToken,
            rewardToken = rewardToken,
            positionFetcher = PositionFetcher(
                stakingContract::stakes,
            ),
        ).nel()
    }

    override fun getProtocol(): Protocol {
        return Protocol.LIQUITY
    }

    override fun getNetwork(): Network {
        return Network.ETHEREUM
    }

}