package io.defitrack.protocol.aave.borrowing

import io.defitrack.borrowing.BorrowService
import io.defitrack.common.network.Network
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.aave.AaveMainnetService
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.math.BigInteger
import java.math.RoundingMode
import java.util.*

@Service
class AaveMainnetBorrowingService(
    private val aaveMainnetService: AaveMainnetService,
) : BorrowService {

    override fun getBorrows(address: String): List<io.defitrack.borrowing.domain.BorrowElement> {
        return aaveMainnetService.getUserReserves(address).mapNotNull {

            if ((it.currentStableDebt > BigInteger.ONE || it.currentVariableDebt > BigInteger.ONE)) {
                io.defitrack.borrowing.domain.BorrowElement(
                    id = UUID.randomUUID().toString(),
                    protocol = getProtocol(),
                    network = getNetwork(),
                    rate = it.reserve.borrowRate,
                    amount = (it.currentStableDebt + it.currentVariableDebt).toBigDecimal()
                        .divide(
                            BigDecimal.TEN.pow(it.reserve.decimals), 2, RoundingMode.HALF_UP
                        ).toPlainString(),
                    name = it.reserve.name,
                    symbol = it.reserve.symbol,
                    tokenUrl = "https://etherscan.io/address/tokens/${it.reserve.underlyingAsset}",
                )
            } else null
        }
    }

    override fun getProtocol(): Protocol = Protocol.AAVE

    override fun getNetwork(): Network = Network.ETHEREUM
}