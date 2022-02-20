package io.defitrack.staking

import io.defitrack.protocol.ProtocolService
import io.defitrack.staking.domain.StakingElement
import io.defitrack.token.ERC20Resource
import io.defitrack.token.FungibleToken
import io.defitrack.token.TokenType
import java.math.BigInteger

abstract class UserStakingService(
    val erC20Resource: ERC20Resource
) : ProtocolService {

    abstract fun getStakings(address: String): List<StakingElement>
    open fun getStaking(address: String, vaultId: String): StakingElement? {
        return getStakings(address).firstOrNull {
            it.id == vaultId
        }
    }

    fun stakingElement(
        vaultName: String,
        rewardTokens: List<FungibleToken>,
        stakedToken: FungibleToken,
        vaultType: String,
        vaultAddress: String,
        rate: Double = 0.0,
        id: String,
        amount: BigInteger
    ): StakingElement {
        return StakingElement(
            id = id,
            network = getNetwork(),
            protocol = getProtocol(),
            name = vaultName,
            rate = rate,
            contractAddress = vaultAddress,
            vaultType = vaultType,
            stakedToken = stakedToken,
            rewardTokens = rewardTokens,
            amount = amount
        )
    }
}