package io.defitrack.staking

import com.fasterxml.jackson.databind.ObjectMapper
import io.defitrack.protocol.ProtocolService
import io.defitrack.staking.domain.RewardToken
import io.defitrack.staking.domain.StakedToken
import io.defitrack.staking.domain.StakingElement
import io.defitrack.token.ERC20Resource
import io.defitrack.token.TokenType
import java.math.BigInteger

abstract class UserStakingService(
    val erC20Resource: ERC20Resource,
    val objectMapper: ObjectMapper
) : ProtocolService {

    abstract fun getStakings(address: String): List<StakingElement>
    open fun getStaking(address: String, vaultId: String): StakingElement? {
        return getStakings(address).firstOrNull {
            it.id == vaultId
        }
    }

    fun stakedToken(
        address: String,
        type: TokenType? = null
    ): StakedToken {
        val token = erC20Resource.getTokenInformation(getNetwork(), address)
        val actualType = type ?: token.type

        return StakedToken(
            address = address,
            network = getNetwork(),
            symbol = token.symbol,
            name = token.name,
            decimals = token.decimals,
            type = actualType
        )
    }

    fun stakingElement(
        user: String,
        vaultUrl: String,
        vaultName: String,
        rewardTokens: List<RewardToken>,
        stakedToken: StakedToken,
        vaultType: String,
        vaultAddress: String,
        rate: Double = 0.0,
        id: String,
        amount: BigInteger
    ): StakingElement {
        return StakingElement(
            id = id,
            network = getNetwork(),
            user = user.lowercase(),
            protocol = getProtocol(),
            name = vaultName,
            rate = rate,
            contractAddress = vaultAddress,
            url = vaultUrl,
            vaultType = vaultType,
            stakedToken = stakedToken,
            rewardTokens = rewardTokens,
            amount = amount
        )
    }
}