package io.defitrack.protocol.application.chainlink

import arrow.core.nel
import io.defitrack.LazyValue
import io.defitrack.common.network.Network
import io.defitrack.common.utils.refreshable
import io.defitrack.architecture.conditional.ConditionalOnCompany
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.market.port.out.FarmingMarketProvider
import io.defitrack.market.domain.farming.FarmingMarket
import io.defitrack.evm.position.PositionFetcher
import io.defitrack.protocol.Company
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.chainlink.ChainlinkStakingContract
import org.springframework.stereotype.Component

@Component
@ConditionalOnCompany(Company.CHAINLINK)
class ChainlinkStakingMarketProvider : FarmingMarketProvider(

) {

    val link = "0x514910771AF9Ca656af840dff83E8264EcF986CA"

    context(BlockchainGateway)
    override suspend fun fetchMarkets(): List<FarmingMarket> {
        val stakingContract = ChainlinkStakingContract(
            "0x3feb1e09b4bb0e7f0387cee092a52e85797ab889"
        )

        val chainlinkToken = getToken(link)
        return create(
            name = "Chainlink Staking",
            identifier = "chainlink_staking",
            stakedToken = chainlinkToken,
            rewardToken = chainlinkToken,
            marketSize = refreshable {
                getMarketSize(
                    chainlinkToken,
                    stakingContract.address,
                )
            },
            type = "chainlink.staking",
            positionFetcher = PositionFetcher(stakingContract::getStake),
        ).nel()
    }

    override fun getProtocol(): Protocol {
        return Protocol.CHAINLINK
    }

    override fun getNetwork(): Network {
        return Network.ETHEREUM
    }
}