package io.defitrack.staking

import com.fasterxml.jackson.databind.ObjectMapper
import io.defitrack.protocol.ProtocolService
import io.defitrack.staking.domain.StakingElement
import io.defitrack.staking.domain.VaultRewardToken
import io.defitrack.staking.domain.VaultStakedToken
import io.defitrack.token.TokenService
import io.defitrack.protocol.staking.TokenType
import java.math.BigInteger
import java.util.*

abstract class UserStakingService(
    val tokenService: TokenService,
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
        val token = tokenService.getTokenInformation(address, getNetwork())
        val asERC20 = tokenService.erc20Resource.getERC20(getNetwork(), address)
        val actualType = type ?: tokenService.getType(asERC20.symbol)

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