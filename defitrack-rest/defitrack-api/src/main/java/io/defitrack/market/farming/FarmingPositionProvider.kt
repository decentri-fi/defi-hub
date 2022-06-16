package io.defitrack.market.farming

import io.defitrack.protocol.ProtocolService
import io.defitrack.market.farming.domain.FarmingPosition
import io.defitrack.market.farming.domain.FarmingMarket
import io.defitrack.token.ERC20Resource
import io.defitrack.token.FungibleToken
import java.math.BigDecimal
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

    fun stakingElement(
        vaultName: String,
        rewardTokens: List<FungibleToken>,
        stakedToken: FungibleToken,
        vaultType: String,
        vaultAddress: String,
        apr: BigDecimal? = null,
        id: String,
        amount: BigInteger
    ): FarmingPosition {
        return FarmingPosition(
            market = FarmingMarket(
                id = id,
                network = getNetwork(),
                protocol = getProtocol(),
                name = vaultName,
                apr = apr,
                contractAddress = vaultAddress,
                vaultType = vaultType,
                stakedToken = stakedToken,
                rewardTokens = rewardTokens,
            ),
            amount = amount
        )
    }
}