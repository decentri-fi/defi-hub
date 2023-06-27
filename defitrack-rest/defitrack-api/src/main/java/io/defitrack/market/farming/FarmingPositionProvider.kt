package io.defitrack.market.farming

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

    abstract suspend fun getStakings(protocol: String, address: String): List<FarmingPosition>
    open suspend fun getStaking(protocol: String, address: String, stakingMarketId: String): FarmingPosition? {
        return getStakings(protocol, address).firstOrNull {
            it.market.id == stakingMarketId
        }
    }

    fun create(
        market: FarmingMarket,
        stakedAmount: BigInteger,
        tokenAmount: BigInteger
    ): FarmingPosition {
        return FarmingPosition(
            market = market,
            underlyingAmount = stakedAmount,
            tokenAmount = tokenAmount
        )
    }
}