package io.defitrack.protocol.aave.borrowing

import io.defitrack.borrowing.BorrowService
import io.defitrack.borrowing.domain.BorrowElement
import io.defitrack.common.network.Network
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.aave.AaveMainnetService
import io.defitrack.token.FungibleToken
import org.springframework.stereotype.Service
import java.math.BigInteger

@Service
class AaveMainnetBorrowingService(
    private val aaveMainnetService: AaveMainnetService,
) : BorrowService {

    override suspend fun getBorrows(address: String): List<BorrowElement> {
        return aaveMainnetService.getUserReserves(address).mapNotNull {
            if ((it.currentStableDebt > BigInteger.ONE || it.currentVariableDebt > BigInteger.ONE)) {
                BorrowElement(
                    id = "aave-ethereum-${it.reserve.id}",
                    protocol = getProtocol(),
                    network = getNetwork(),
                    rate = it.reserve.borrowRate,
                    amount = (it.currentStableDebt + it.currentVariableDebt),
                    name = "Aave ${it.reserve.name} Borrow",
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

    override fun getNetwork(): Network = Network.ETHEREUM
}