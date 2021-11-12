package io.codechef.defitrack.staking.vo

import java.math.BigDecimal

data class VaultStakedTokenVO(
    var name: String = "?",
    var symbol: String = "?",
    var decimals: Int = 18,
    var amount: BigDecimal
)