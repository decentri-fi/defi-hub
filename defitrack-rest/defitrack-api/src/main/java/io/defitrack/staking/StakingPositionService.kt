package io.defitrack.staking

import io.defitrack.protocol.ProtocolService
import io.defitrack.staking.domain.StakingPosition
import io.defitrack.staking.domain.StakingMarket
import io.defitrack.token.ERC20Resource
import io.defitrack.token.FungibleToken
import java.math.BigDecimal
import java.math.BigInteger

abstract class StakingPositionService(
    val erC20Resource: ERC20Resource
) : ProtocolService {

    abstract suspend fun getStakings(address: String): List<StakingPosition>
    open suspend fun getStaking(address: String, stakingMarketId: String): StakingPosition? {
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
    ): StakingPosition {
        return StakingPosition(
            market = StakingMarket(
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