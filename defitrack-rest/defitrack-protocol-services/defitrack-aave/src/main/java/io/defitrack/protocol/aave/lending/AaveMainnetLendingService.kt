package io.defitrack.protocol.aave.lending

import io.defitrack.common.network.Network
import io.defitrack.lending.LendingService
import io.defitrack.lending.domain.LendingElement
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.aave.AaveMainnetService
import io.defitrack.token.FungibleToken
import org.springframework.stereotype.Service
import java.math.BigInteger

@Service
class AaveMainnetLendingService(
    private val aaveMainnetService: AaveMainnetService,
) : LendingService {

    override fun getProtocol(): Protocol = Protocol.AAVE

    override fun getNetwork(): Network = Network.ETHEREUM

    override suspend fun getLendings(address: String): List<LendingElement> {
        return aaveMainnetService.getUserReserves(address).mapNotNull {
            if (it.currentATokenBalance > BigInteger.ZERO) {
                LendingElement(
                    id = "ethereum-aave-${it.reserve.symbol}",
                    protocol = getProtocol(),
                    network = getNetwork(),
                    rate = it.reserve.lendingRate,
                    amount = it.currentATokenBalance,
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
}