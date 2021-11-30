package io.defitrack.staking.vo

import io.defitrack.staking.domain.VaultRewardToken

data class VaultRewardTokenVO(
    var name: String = "",
    var symbol: String = "",
    var decimals: Int = 18,
    var daily: String = ""
)

fun VaultRewardToken.toVO(): VaultRewardTokenVO = VaultRewardTokenVO(
    name = this.name,
    symbol = this.symbol,
    decimals = this.decimals,
    daily = this.daily
)