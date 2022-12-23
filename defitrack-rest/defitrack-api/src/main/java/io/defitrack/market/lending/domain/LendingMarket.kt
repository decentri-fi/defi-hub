package io.defitrack.market.lending.domain

import io.defitrack.common.network.Network
import io.defitrack.evm.contract.multicall.MultiCallElement
import io.defitrack.market.DefiMarket
import io.defitrack.market.farming.domain.InvestmentPreparer
import io.defitrack.protocol.Protocol
import io.defitrack.token.FungibleToken
import org.web3j.abi.datatypes.Type
import java.math.BigDecimal
import java.math.BigInteger


data class LendingMarket(
    val id: String,
    val network: Network,
    val protocol: Protocol,
    val name: String,
    val token: FungibleToken,
    val marketSize: BigDecimal? = null,
    val rate: BigDecimal? = null,
    val poolType: String,
    val positionFetcher: PositionFetcher? = null,
    val investmentPreparer: InvestmentPreparer? = null
) : DefiMarket

class PositionFetcher(
    val address: String,
    val function: (user: String) -> org.web3j.abi.datatypes.Function,
    val extractBalance: suspend (List<Type<*>>) -> BigInteger = { result ->
        try {
            result[0].value as BigInteger
        } catch (ex: Exception) {
            ex.printStackTrace()
            BigInteger.ZERO
        }
    }
) {
    fun toMulticall(user: String): MultiCallElement {
        return MultiCallElement(
            function(user), address
        )
    }
}