package io.defitrack.protocol.application.aave.v2.borrowing

import io.defitrack.architecture.conditional.ConditionalOnCompany
import io.defitrack.common.network.Network
import io.defitrack.market.domain.borrow.BorrowMarket
import io.defitrack.market.domain.borrow.BorrowPosition
import io.defitrack.market.port.out.BorrowPositionProvider
import io.defitrack.port.output.ERC20Client
import io.defitrack.protocol.Company
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.aave.v2.AaveV2PolygonService
import org.springframework.stereotype.Component
import java.math.BigInteger

@Component
@ConditionalOnCompany(Company.AAVE)
class AavePolygonBorrowingPositionProvider(
    private val aaveV2PolygonService: AaveV2PolygonService,
    private val erC20Resource: ERC20Client
) : BorrowPositionProvider {

    override suspend fun getPositions(address: String): List<BorrowPosition> {
        return aaveV2PolygonService.getUserReserves(address).mapNotNull {
            if ((it.currentStableDebt > BigInteger.ONE || it.currentVariableDebt > BigInteger.ONE)) {
                val token = erC20Resource.getTokenInformation(getNetwork(), it.reserve.underlyingAsset)
                BorrowPosition(
                    market = BorrowMarket(
                        id = "aave-polygon-${it.reserve.id}",
                        protocol = getProtocol(),
                        network = getNetwork(),
                        rate = it.reserve.borrowRate.toBigDecimal(),
                        deprecated = false,
                        name = "Aave ${token.name} Borrow",
                        token = token,
                        type = "aave-borrowing",
                    ),
                    tokenAmount = (it.currentStableDebt + it.currentVariableDebt),
                    underlyingAmount = (it.currentStableDebt + it.currentVariableDebt),
                )
            } else null
        }
    }

    override fun getProtocol(): Protocol = Protocol.AAVE_V2

    override fun getNetwork(): Network = Network.POLYGON
}