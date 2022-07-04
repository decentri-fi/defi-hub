package io.defitrack.market.farming

import io.defitrack.protocol.ProtocolService
import io.defitrack.market.farming.domain.FarmingPosition
import io.defitrack.market.farming.domain.FarmingMarket
import io.defitrack.token.ERC20Resource
import java.math.BigInteger

abstract class FarmingPositionProvider(
    val erC20Resource: ERC20Resource
) : ProtocolService {

    abstract suspend fun getStakings(address: String): List<FarmingPosition>
    open suspend fun getStaking(address: String, stakingMarketId: String): FarmingPosition? {
        return getStakings(address).firstOrNull {
            it.market.id == stakingMarketId
        }
    }

    fun create(
        market: FarmingMarket,
        amount: BigInteger
    ): FarmingPosition {
        return FarmingPosition(
            market = market,
            amount = amount
        )
    }
}