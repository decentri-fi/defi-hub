package io.defitrack.market.port.out

import io.defitrack.evm.contract.BlockchainGatewayProvider
import io.defitrack.market.domain.farming.FarmingMarket
import io.defitrack.market.domain.farming.FarmingPosition
import io.defitrack.port.output.ERC20Client
import org.springframework.beans.factory.annotation.Autowired
import java.math.BigInteger

abstract class FarmingPositionProvider {

    @Autowired
    lateinit var erC20Resource: ERC20Client

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