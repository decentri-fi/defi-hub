package io.defitrack.lending.domain

import io.defitrack.common.network.Network
import io.defitrack.evm.contract.multicall.MultiCallElement
import io.defitrack.protocol.Protocol
import io.defitrack.staking.domain.InvestmentPreparer
import io.defitrack.token.FungibleToken
import org.web3j.abi.datatypes.Type
import java.math.BigDecimal
import java.math.BigInteger


data class LendingMarket(
    val id: String,
    val network: Network,
    val protocol: Protocol,
    val address: String,
    val name: String,
    val token: FungibleToken,
    val marketSize: BigDecimal? = null,
    val rate: BigDecimal? = null,
    val poolType: String,
    val balanceFetcher: BalanceFetcher? = null,
    val investmentPreparer: InvestmentPreparer? = null
)

class BalanceFetcher(
    val address: String,
    val function: (user: String) -> org.web3j.abi.datatypes.Function,
    val extractBalance: (List<Type<*>>) -> BigInteger = { result ->
        result[0].value as BigInteger
    }
) {
    fun toMulticall(user: String): MultiCallElement {
        return MultiCallElement(
            function(user), address
        )
    }
}