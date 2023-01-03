package io.defitrack.market.farming

import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.BlockchainGatewayProvider
import io.defitrack.market.farming.domain.FarmingMarket
import io.defitrack.market.farming.domain.FarmingPosition
import io.defitrack.token.ERC20Resource
import org.springframework.beans.factory.annotation.Autowired
import java.math.BigInteger

abstract class FarmingPositionProvider {

    @Autowired
    lateinit var erC20Resource: ERC20Resource
    @Autowired
    lateinit var blockchainGatewayProvider: BlockchainGatewayProvider

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