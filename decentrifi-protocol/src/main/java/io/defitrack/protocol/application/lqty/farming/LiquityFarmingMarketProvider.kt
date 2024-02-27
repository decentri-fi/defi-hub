package io.defitrack.protocol.application.lqty.farming

import arrow.core.nonEmptyListOf
import io.defitrack.claim.ClaimableRewardFetcher
import io.defitrack.claim.Reward
import io.defitrack.common.network.Network
import io.defitrack.architecture.conditional.ConditionalOnCompany
import io.defitrack.market.port.out.FarmingMarketProvider
import io.defitrack.market.domain.farming.FarmingMarket
import io.defitrack.evm.position.PositionFetcher
import io.defitrack.protocol.Company
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.liquity.LiquityStabilityPoolContract
import io.defitrack.transaction.PreparedTransaction.Companion.selfExecutingTransaction
import org.springframework.stereotype.Component

@ConditionalOnCompany(Company.LIQUITY)
@Component
class LiquityFarmingMarketProvider : FarmingMarketProvider() {

    val address = "0x66017d22b0f8556afdd19fc67041899eb65a21bb"

    val lusdAddress = "0x5f98805a4e8be255a32880fdec7f6728c6568ba0"
    val liquityAddress = "0x6dea81c8171d0ba574754ef6f8b412f2ed88c54d"


    override suspend fun fetchMarkets(): List<FarmingMarket> {
        val contract = LiquityStabilityPoolContract(getBlockchainGateway(), address)

        val lusd = getToken(lusdAddress)
        val liquity = getToken(liquityAddress)
        val eth = getToken("0x0")

        return listOf(
            create(
                name = "LUSD Farm",
                identifier = address,
                stakedToken = lusd,
                rewardTokens = nonEmptyListOf(liquity, eth),
                positionFetcher = PositionFetcher(contract::deposits),
                claimableRewardFetcher = ClaimableRewardFetcher(
                    listOf(
                        Reward(
                            liquity,
                            contract::lqtyGain
                        ),
                        Reward(
                            eth,
                            contract::ethGain
                        )
                    ),
                    selfExecutingTransaction(contract::claim)
                )
            )
        )
    }

    override fun getProtocol(): Protocol {
        return Protocol.LIQUITY
    }

    override fun getNetwork(): Network {
        return Network.ETHEREUM
    }
}