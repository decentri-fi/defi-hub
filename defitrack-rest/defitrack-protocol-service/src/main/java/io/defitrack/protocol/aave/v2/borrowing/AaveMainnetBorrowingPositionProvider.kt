package io.defitrack.protocol.aave.v2.borrowing

import io.defitrack.common.network.Network
import io.defitrack.architecture.conditional.ConditionalOnCompany
import io.defitrack.erc20.port.`in`.ERC20Resource
import io.defitrack.market.port.out.BorrowPositionProvider
import io.defitrack.market.domain.borrow.BorrowMarket
import io.defitrack.market.domain.borrow.BorrowPosition
import io.defitrack.protocol.Company
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.aave.v2.AaveV2MainnetService
import org.springframework.stereotype.Component
import java.math.BigInteger

@Component
@ConditionalOnCompany(Company.AAVE)
class AaveMainnetBorrowingPositionProvider(
    private val aaveV2MainnetService: AaveV2MainnetService,
    private val erC20Resource: ERC20Resource
) : BorrowPositionProvider {

    override suspend fun getPositions(address: String): List<BorrowPosition> {
        return aaveV2MainnetService.getUserReserves(address).mapNotNull {
            if ((it.currentStableDebt > BigInteger.ONE || it.currentVariableDebt > BigInteger.ONE)) {

                val token = erC20Resource.getTokenInformation(getNetwork(), it.reserve.underlyingAsset)

                BorrowPosition(
                    market = BorrowMarket(
                        id = "aave-ethereum-${it.reserve.id}",
                        protocol = getProtocol(),
                        network = getNetwork(),
                        rate = it.reserve.borrowRate.toBigDecimal(),
                        deprecated = false,
                        name = "Aave ${token.name} Borrow",
                        token = token
                    ),
                    tokenAmount = (it.currentStableDebt + it.currentVariableDebt),
                    underlyingAmount = (it.currentStableDebt + it.currentVariableDebt),
                )
            } else null
        }
    }

    override fun getProtocol(): Protocol = Protocol.AAVE_V2

    override fun getNetwork(): Network = Network.ETHEREUM
}