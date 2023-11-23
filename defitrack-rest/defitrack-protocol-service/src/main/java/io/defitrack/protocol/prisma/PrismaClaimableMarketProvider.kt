package io.defitrack.protocol.prisma

import arrow.fx.coroutines.parMapNotNull
import io.defitrack.claimable.ClaimableMarketProvider
import io.defitrack.claimable.domain.ClaimableMarket
import io.defitrack.claimable.domain.ClaimableRewardFetcher
import io.defitrack.claimable.domain.Reward
import io.defitrack.common.network.Network
import io.defitrack.conditional.ConditionalOnCompany
import io.defitrack.protocol.Company
import io.defitrack.transaction.PreparedTransaction.Companion.selfExecutingTransaction
import org.springframework.stereotype.Component

@Component
@ConditionalOnCompany(Company.PRISMA)
class PrismaClaimableMarketProvider(
    private val prismaLendingMarketProvider: PrismaLendingMarketProvider
) : ClaimableMarketProvider() {

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