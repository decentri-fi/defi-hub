package io.defitrack.protocol.lqty.lending

import arrow.core.nel
import io.defitrack.common.network.Network
import io.defitrack.common.utils.FormatUtilsExtensions.asEth
import io.defitrack.common.utils.Refreshable.Companion.refreshable
import io.defitrack.conditional.ConditionalOnCompany
import io.defitrack.market.lending.LendingMarketProvider
import io.defitrack.market.lending.domain.LendingMarket
import io.defitrack.evm.position.PositionFetcher
import io.defitrack.protocol.Company
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.liquity.TroveManagerContract
import org.springframework.stereotype.Component

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
                troveManager.totalCollateralSnapshot.await().asEth()
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