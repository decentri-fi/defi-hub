package io.defitrack.protocol.prisma

import arrow.fx.coroutines.parMapNotNull
import io.defitrack.common.network.Network
import io.defitrack.common.utils.FormatUtilsExtensions.asEth
import io.defitrack.common.utils.Refreshable.Companion.refreshable
import io.defitrack.conditional.ConditionalOnCompany
import io.defitrack.market.lending.LendingMarketProvider
import io.defitrack.market.lending.domain.LendingMarket
import io.defitrack.evm.position.PositionFetcher
import io.defitrack.protocol.Company
import io.defitrack.protocol.Protocol
import org.springframework.stereotype.Component

@Component
@ConditionalOnCompany(Company.PRISMA)
class PrismaLendingMarketProvider : LendingMarketProvider() {

    val troveManagers = listOf(
        "0xbf6883a03fd2fcfa1b9fc588ad6193b3c3178f8f",
        "0xf69282a7e7ba5428f92f610e7afa1c0cedc4e483",
        "0xe0e255fd5281bec3bb8fa1569a20097d9064e445",
        "0x63cc74334f4b1119276667cf0079ac0c8a96cfb2"
    )

    override suspend fun fetchMarkets(): List<LendingMarket> {
        val contracts = troveManagers
            .map {
                TroveManagerContract(getBlockchainGateway(), it)
            }
        return resolve(contracts).parMapNotNull { troveManager ->

            val collateral = getToken(troveManager.collateralToken.await())

            create(
                identifier = troveManager.address,
                name = "Prisma Lending",
                token = collateral,
                poolType = "prisma",
                totalSupply = refreshable {
                    troveManager.totalCollateralSnapshot.await().asEth()
                },
                positionFetcher = PositionFetcher(
                    troveManager::getTroveCollAndDebt,
                ),
                internalMetaData = mapOf(
                    "contract" to troveManager
                )
            )
        }
    }

    override fun getProtocol(): Protocol {
        return Protocol.PRISMA
    }

    override fun getNetwork(): Network {
        return Network.ETHEREUM
    }
}