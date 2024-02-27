package io.defitrack.protocol.application.prisma

import arrow.core.Either
import arrow.core.Either.Companion.catch
import arrow.core.getOrElse
import arrow.fx.coroutines.parMapNotNull
import io.defitrack.common.network.Network
import io.defitrack.common.utils.FormatUtilsExtensions.asEth
import io.defitrack.common.utils.refreshable
import io.defitrack.architecture.conditional.ConditionalOnCompany
import io.defitrack.evm.position.PositionFetcher
import io.defitrack.market.port.out.LendingMarketProvider
import io.defitrack.market.domain.lending.LendingMarket
import io.defitrack.protocol.Company
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.prisma.TroveManagerContract
import org.springframework.stereotype.Component
import java.math.BigDecimal

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
        return troveManagers
            .map(::troveManagerContract)
            .resolve()
            .parMapNotNull { troveManager ->

                create(
                    identifier = troveManager.address,
                    name = "Prisma Lending",
                    token = getToken(troveManager.collateralToken.await()),
                    poolType = "prisma",
                    totalSupply = refreshable {
                        catch {
                            troveManager.totalCollateralSnapshot.await().asEth()
                        }.mapLeft {
                            logger.error("Error while fetching prisma market", it)
                        }.getOrElse { BigDecimal.ZERO }
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

    private fun troveManagerContract(it: String): TroveManagerContract = with(getBlockchainGateway()) {
        TroveManagerContract(it)
    }

    override fun getProtocol(): Protocol {
        return Protocol.PRISMA
    }

    override fun getNetwork(): Network {
        return Network.ETHEREUM
    }
}