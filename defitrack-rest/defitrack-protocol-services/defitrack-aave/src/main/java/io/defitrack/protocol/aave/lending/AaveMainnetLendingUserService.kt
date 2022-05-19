package io.defitrack.protocol.aave.lending

import io.defitrack.common.network.Network
import io.defitrack.lending.LendingUserService
import io.defitrack.lending.domain.LendingMarket
import io.defitrack.lending.domain.LendingPosition
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.aave.AaveMainnetService
import io.defitrack.token.ERC20Resource
import org.springframework.stereotype.Service
import java.math.BigInteger

@Service
class AaveMainnetLendingUserService(
    private val aaveMainnetService: AaveMainnetService,
    private val erC20Resource: ERC20Resource
) : LendingUserService {

    override fun getProtocol(): Protocol = Protocol.AAVE

    override fun getNetwork(): Network = Network.ETHEREUM

    override suspend fun getLendings(address: String): List<LendingPosition> {
        return aaveMainnetService.getUserReserves(address).mapNotNull {
            if (it.currentATokenBalance > BigInteger.ZERO) {
                val token = erC20Resource.getTokenInformation(getNetwork(), it.reserve.underlyingAsset)
                LendingPosition(
                    market = LendingMarket(
                        id = "ethereum-aave-${it.reserve.symbol}",
                        protocol = getProtocol(),
                        network = getNetwork(),
                        rate = it.reserve.lendingRate.toBigDecimal(),
                        name = it.reserve.name,
                        token = token.toFungibleToken(),
                        address = it.reserve.id,
                        poolType = "aave-lending"
                    ),
                    amount = it.currentATokenBalance,
                )
            } else null
        }
    }
}