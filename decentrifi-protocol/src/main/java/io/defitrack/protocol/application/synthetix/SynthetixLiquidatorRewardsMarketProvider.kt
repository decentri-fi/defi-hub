package io.defitrack.protocol.application.synthetix

import arrow.core.nel
import io.defitrack.architecture.conditional.ConditionalOnCompany
import io.defitrack.architecture.conditional.ConditionalOnNetwork
import io.defitrack.claim.*
import io.defitrack.common.network.Network
import io.defitrack.protocol.Company
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.synthetix.LiquidatorRewardsContract
import io.defitrack.transaction.PreparedTransaction.Companion.selfExecutingTransaction
import org.springframework.stereotype.Component

@Component
@ConditionalOnCompany(Company.SYNTHETIX)
@ConditionalOnNetwork(Network.OPTIMISM)
class SynthetixLiquidatorRewardsMarketProvider : AbstractClaimableMarketProvider() {


    val liquidqtorContractAddress = "0xf4eebdd0704021ef2a6bbe993fdf93030cd784b4"
    val snxAddress = "0x8700daec35af8ff88c16bdf0418774cb3d7599b4"
    override suspend fun fetchClaimables(): List<ClaimableMarket> {
        val contract =
            LiquidatorRewardsContract(blockchainGatewayProvider.getGateway(Network.OPTIMISM), liquidqtorContractAddress)

        val rewardToken = erC20Resource.getTokenInformation(Network.OPTIMISM, snxAddress)

        return ClaimableMarket(
            id = liquidqtorContractAddress,
            name = "Synthetix Liquidator Rewards",
            network = Network.OPTIMISM,
            protocol = Protocol.SYNTHETIX,
            claimableRewardFetchers = listOf(
                ClaimableRewardFetcher(
                    reward = Reward(
                        rewardToken,
                        contract::earnedFn
                    ),
                    selfExecutingTransaction(contract.getRewardFn())
                )
            )
        ).nel()
    }
}