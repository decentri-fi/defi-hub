package io.defitrack.staking

import com.fasterxml.jackson.databind.ObjectMapper
import io.defitrack.protocol.ProtocolService
import io.defitrack.token.TokenType
import io.defitrack.staking.domain.StakingElement
import io.defitrack.staking.domain.VaultRewardToken
import io.defitrack.staking.domain.VaultStakedToken
import io.defitrack.token.ERC20Resource
import java.math.BigInteger
import java.util.*

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

    fun vaultStakedToken(
        address: String,
        amount: BigInteger,
        type: TokenType? = null
    ): VaultStakedToken {
        val token = erC20Resource.getTokenInformation(getNetwork(), address)
        val actualType = type ?: token.type

        return VaultStakedToken(
            address,
            getNetwork(),
            amount,
            token.symbol,
            token.name,
            token.decimals,
            type = actualType
        )
    }

    fun stakingElement(
        user: String,
        vaultUrl: String,
        vaultName: String,
        rewardTokens: List<VaultRewardToken>,
        stakedToken: VaultStakedToken,
        vaultType: String,
        vaultAddress: String,
        rate: Double = 0.0,
        id: String = UUID.randomUUID().toString()
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
            rewardTokens = rewardTokens
        )
    }
}