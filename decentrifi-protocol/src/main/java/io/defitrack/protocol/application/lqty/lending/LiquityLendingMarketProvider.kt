package io.defitrack.protocol.application.lqty.lending

import arrow.core.Either
import arrow.core.getOrElse
import arrow.core.nel
import io.defitrack.common.network.Network
import io.defitrack.common.utils.FormatUtilsExtensions.asEth
import io.defitrack.common.utils.refreshable
import io.defitrack.architecture.conditional.ConditionalOnCompany
import io.defitrack.evm.position.PositionFetcher
import io.defitrack.market.port.out.LendingMarketProvider
import io.defitrack.market.domain.lending.LendingMarket
import io.defitrack.protocol.Company
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.liquity.TroveManagerContract
import org.springframework.stereotype.Component
import java.math.BigDecimal

@Component
@ConditionalOnCompany(Company.LIQUITY)
class LiquityLendingMarketProvider(
) : LendingMarketProvider() {

    val troveManagerAddress = "0xa39739ef8b0231dbfa0dcda07d7e29faabcf4bb2"

    override suspend fun fetchMarkets(): List<LendingMarket> {

        val troveManager = TroveManagerContract(getBlockchainGateway(), troveManagerAddress)
        val eth = getToken("0x0")

        return create(
            identifier = troveManagerAddress,
            name = "Liquity Lending",
            token = eth,
            poolType = "liquity",
            marketToken = null,
            totalSupply = refreshable {
                Either.catch {
                    troveManager.totalCollateralSnapshot.await().asEth()
                }.mapLeft {
                    logger.error("Unable to get total collateral snapshot: {}", it.message)
                }.getOrElse { BigDecimal.ZERO }
            },
            positionFetcher = PositionFetcher(
                troveManager::getTroveColl,
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