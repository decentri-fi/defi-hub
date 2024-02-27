package io.defitrack.protocol.application.prisma

import arrow.fx.coroutines.parMapNotNull
import io.defitrack.claim.AbstractClaimableMarketProvider
import io.defitrack.claim.ClaimableMarket
import io.defitrack.claim.ClaimableRewardFetcher
import io.defitrack.claim.Reward
import io.defitrack.common.network.Network
import io.defitrack.architecture.conditional.ConditionalOnCompany
import io.defitrack.protocol.Company
import io.defitrack.protocol.prisma.TroveManagerContract
import io.defitrack.transaction.PreparedTransaction.Companion.selfExecutingTransaction
import org.springframework.stereotype.Component

@Component
@ConditionalOnCompany(Company.PRISMA)
class PrismaClaimableMarketProvider(
    private val prismaLendingMarketProvider: PrismaLendingMarketProvider
) : AbstractClaimableMarketProvider() {

    val prismaAddress = "0xda47862a83dac0c112ba89c6abc2159b95afd71c"

    override suspend fun fetchClaimables(): List<ClaimableMarket> {
        val prisma = erC20Resource.getTokenInformation(Network.ETHEREUM, prismaAddress)

        return prismaLendingMarketProvider.getMarkets().parMapNotNull {
            val contract = it.internalMetaData["contract"] as TroveManagerContract
            ClaimableMarket(
                id = contract.address,
                name = it.name + " reward",
                network = it.network,
                protocol = it.protocol,
                claimableRewardFetchers = listOf(
                    ClaimableRewardFetcher(
                        Reward(
                            prisma,
                            contract::claimableReward,
                        ),
                        preparedTransaction = selfExecutingTransaction(contract::claimFn)
                    ),
                )
            )
        }
    }
}