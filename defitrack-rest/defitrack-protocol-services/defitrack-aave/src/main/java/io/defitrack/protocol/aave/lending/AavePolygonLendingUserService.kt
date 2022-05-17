package io.defitrack.protocol.aave.lending

import io.defitrack.common.network.Network
import io.defitrack.lending.LendingUserService
import io.defitrack.lending.domain.LendingElement
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.aave.AavePolygonService
import io.defitrack.token.ERC20Resource
import org.springframework.stereotype.Service
import java.math.BigInteger

@Service
class AavePolygonLendingUserService(
    private val aavePolygonService: AavePolygonService,
    private val erC20Resource: ERC20Resource
) : LendingUserService {

    override fun getProtocol(): Protocol = Protocol.AAVE

    override fun getNetwork(): Network = Network.POLYGON

    override suspend fun getLendings(address: String): List<LendingElement> {
        return aavePolygonService.getUserReserves(address).mapNotNull {
            if (it.currentATokenBalance > BigInteger.ZERO) {
                val token = erC20Resource.getTokenInformation(getNetwork(), it.reserve.underlyingAsset)
                LendingElement(
                    id = "polygon-aave-${it.reserve.symbol}",
                    protocol = getProtocol(),
                    network = getNetwork(),
                    rate = it.reserve.lendingRate,
                    amount = it.currentATokenBalance,
                    name = it.reserve.name,
                    token = token.toFungibleToken()
                )
            } else null
        }
    }
}