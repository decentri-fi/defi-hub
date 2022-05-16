package io.defitrack.staking

import io.defitrack.protocol.ProtocolService
import io.defitrack.staking.domain.StakingElement
import io.defitrack.staking.domain.StakingMarketElement
import io.defitrack.token.ERC20Resource
import io.defitrack.token.FungibleToken
import java.math.BigDecimal
import java.math.BigInteger

abstract class UserStakingService(
    val erC20Resource: ERC20Resource
) : ProtocolService {

    abstract fun getStakings(address: String): List<StakingElement>
    open fun getStaking(address: String, stakingMarketId: String): StakingElement? {
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
    ): StakingElement {
        return StakingElement(
            market = StakingMarketElement(
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