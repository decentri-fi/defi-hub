package io.defitrack.protocol.application.prisma

import arrow.core.nel
import io.defitrack.architecture.conditional.ConditionalOnCompany
import io.defitrack.architecture.conditional.ConditionalOnNetwork
import io.defitrack.claim.ClaimableRewardFetcher
import io.defitrack.claim.Reward
import io.defitrack.common.network.Network
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.position.PositionFetcher
import io.defitrack.market.domain.farming.FarmingMarket
import io.defitrack.market.port.out.FarmingMarketProvider
import io.defitrack.protocol.Company
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.prisma.StabilityPoolContract
import io.defitrack.transaction.PreparedTransaction.Companion.selfExecutingTransaction
import org.springframework.stereotype.Component

@Component
@ConditionalOnCompany(Company.PRISMA)
@ConditionalOnNetwork(Network.ETHEREUM)
class PrismaFarmingMarketProvider : FarmingMarketProvider() {

    val stabilityPoolAddress = "0xed8b26d99834540c5013701bb3715fafd39993ba"
    val mkUsdAddress = "0x4591dbff62656e7859afe5e45f6f47d3669fbb28"
    val prismaAddress = "0xda47862a83dac0c112ba89c6abc2159b95afd71c"

    context(BlockchainGateway)
    override suspend fun fetchMarkets(): List<FarmingMarket> {
        val contract = StabilityPoolContract(stabilityPoolAddress)

        val prisma = getToken(prismaAddress)

        return create(
            name = "mkUSD Stability Pool",
            identifier = stabilityPoolAddress,
            stakedToken = getToken(mkUsdAddress),
            rewardToken = prisma,
            positionFetcher = PositionFetcher(contract::accountDeposits),
            type = "prisma.farming",
            claimableRewardFetcher = ClaimableRewardFetcher(
                reward = Reward(
                    prisma,
                    contract::claimableReward
                ),
                preparedTransaction = selfExecutingTransaction(contract::claimFn)
            )
        ).nel()
    }

    override fun getProtocol(): Protocol {
        return Protocol.PRISMA
    }

    override fun getNetwork(): Network {
        return Network.ETHEREUM
    }

}