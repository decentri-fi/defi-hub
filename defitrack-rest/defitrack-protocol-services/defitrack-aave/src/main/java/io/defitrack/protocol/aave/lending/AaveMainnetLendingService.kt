package io.defitrack.protocol.aave.lending

import io.codechef.defitrack.lending.LendingService
import io.codechef.defitrack.lending.domain.LendingElement
import io.defitrack.common.network.Network
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.aave.AaveMainnetService
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.math.BigInteger
import java.math.RoundingMode

@Service
class AaveMainnetLendingService(
    private val aaveMainnetService: AaveMainnetService,
) : LendingService {

    override fun getProtocol(): Protocol = Protocol.AAVE

    override fun getNetwork(): Network = Network.ETHEREUM

    override fun getLendings(address: String): List<LendingElement> {
        return aaveMainnetService.getUserReserves(address).mapNotNull {
            if (it.currentATokenBalance > BigInteger.ZERO) {
                LendingElement(
                    user = address.lowercase(),
                    id = "ethereum-aave-${it.reserve.symbol}",
                    protocol = getProtocol(),
                    network = getNetwork(),
                    rate = it.reserve.lendingRate,
                    amount = (it.currentATokenBalance).toBigDecimal()
                        .divide(
                            BigDecimal.TEN.pow(it.reserve.decimals), 6, RoundingMode.HALF_UP
                        ).toPlainString(),
                    name = it.reserve.name,
                    symbol = it.reserve.symbol,
                )
            } else null
        }
    }
}