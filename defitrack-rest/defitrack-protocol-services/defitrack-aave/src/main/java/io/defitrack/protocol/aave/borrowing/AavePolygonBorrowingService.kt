package io.defitrack.protocol.aave.borrowing

import io.defitrack.borrowing.BorrowService
import io.defitrack.borrowing.domain.BorrowElement
import io.defitrack.common.network.Network
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.aave.AavePolygonService
import io.defitrack.token.FungibleToken
import org.springframework.stereotype.Service
import java.math.BigInteger

@Service
class AavePolygonBorrowingService(
    private val aavePolygonService: AavePolygonService,
) : BorrowService {

    override suspend fun getBorrows(address: String): List<BorrowElement> {
        return aavePolygonService.getUserReserves(address).mapNotNull {
            if ((it.currentStableDebt > BigInteger.ONE || it.currentVariableDebt > BigInteger.ONE)) {
                BorrowElement(
                    id = "aave-polygon-${it.reserve.id}",
                    protocol = getProtocol(),
                    network = getNetwork(),
                    rate = it.reserve.borrowRate,
                    amount = (it.currentStableDebt + it.currentVariableDebt),
                    name = it.reserve.name,
                    token = FungibleToken(
                        symbol = it.reserve.symbol,
                        name = it.reserve.name,
                        decimals = it.reserve.decimals
                    )
                )
            } else null
        }
    }

    override fun getProtocol(): Protocol = Protocol.AAVE

    override fun getNetwork(): Network = Network.POLYGON
}