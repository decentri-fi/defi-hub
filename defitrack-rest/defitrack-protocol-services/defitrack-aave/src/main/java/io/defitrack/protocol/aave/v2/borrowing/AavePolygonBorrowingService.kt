package io.defitrack.protocol.aave.v2.borrowing

import io.defitrack.market.borrowing.BorrowService
import io.defitrack.market.borrowing.domain.BorrowPosition
import io.defitrack.common.network.Network
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.aave.v2.AaveV2PolygonService
import io.defitrack.token.ERC20Resource
import org.springframework.stereotype.Service
import java.math.BigInteger

@Service
class AavePolygonBorrowingService(
    private val aaveV2PolygonService: AaveV2PolygonService,
    private val erC20Resource: ERC20Resource
) : BorrowService {

    override suspend fun getBorrows(address: String): List<BorrowPosition> {
        return aaveV2PolygonService.getUserReserves(address).mapNotNull {
            if ((it.currentStableDebt > BigInteger.ONE || it.currentVariableDebt > BigInteger.ONE)) {
                val token = erC20Resource.getTokenInformation(getNetwork(), it.reserve.underlyingAsset)
                BorrowPosition(
                    id = "aave-polygon-${it.reserve.id}",
                    protocol = getProtocol(),
                    network = getNetwork(),
                    rate = it.reserve.borrowRate,
                    amount = (it.currentStableDebt + it.currentVariableDebt),
                    name = it.reserve.name,
                    token = token.toFungibleToken()
                )
            } else null
        }
    }

    override fun getProtocol(): Protocol = Protocol.AAVE

    override fun getNetwork(): Network = Network.POLYGON
}