package io.codechef.protocol.aave.lending

import io.codechef.common.network.Network
import io.codechef.defitrack.lending.LendingService
import io.codechef.defitrack.lending.domain.LendingElement
import io.codechef.protocol.Protocol
import io.codechef.protocol.aave.AavePolygonService
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.math.BigInteger
import java.math.RoundingMode

@Service
class AavePolygonLendingService(
    private val aavePolygonService: AavePolygonService
) : LendingService {

    override fun getProtocol(): Protocol = Protocol.AAVE

    override fun getNetwork(): Network = Network.POLYGON

    override fun getLendings(address: String): List<LendingElement> {
        return aavePolygonService.getUserReserves(address).mapNotNull {

            if (it.currentATokenBalance > BigInteger.ZERO) {
                LendingElement(
                    user = address.lowercase(),
                    id = "polygon-aave-${it.reserve.symbol}",
                    protocol = getProtocol(),
                    network = getNetwork(),
                    rate = it.reserve.lendingRate,
                    amount = (it.currentATokenBalance).toBigDecimal()
                        .divide(
                            BigDecimal.TEN.pow(it.reserve.decimals), 6, RoundingMode.HALF_UP
                        ).toPlainString(),
                    name = it.reserve.name,
                    symbol = it.reserve.symbol
                )
            } else null
        }
    }
}