package io.defitrack.market.lending.domain

import io.defitrack.common.network.Network
import io.defitrack.evm.contract.multicall.MultiCallElement
import io.defitrack.exit.ExitPositionPreparer
import io.defitrack.market.DefiMarket
import io.defitrack.market.farming.domain.InvestmentPreparer
import io.defitrack.protocol.Protocol
import io.defitrack.token.FungibleToken
import org.web3j.abi.datatypes.Type
import java.math.BigDecimal
import java.math.BigInteger


data class LendingMarket(
    override val id: String,
    val network: Network,
    val protocol: Protocol,
    val name: String,
    val token: FungibleToken,
    val marketSize: BigDecimal? = null,
    val rate: BigDecimal? = null,
    val poolType: String,
    val positionFetcher: PositionFetcher? = null,
    val investmentPreparer: InvestmentPreparer? = null,
    val exitPositionPreparer: ExitPositionPreparer? = null,
    val metadata: Map<String, Any> = emptyMap(),
    val erc20Compatible: Boolean = false,
    val totalSupply: BigDecimal,
    val price: BigDecimal
) : DefiMarket

class PositionFetcher(
    val address: String,
    val function: (user: String) -> org.web3j.abi.datatypes.Function,
    val extractBalance: suspend (List<Type<*>>) -> Position = { result ->
        try {
            val result = result[0].value as BigInteger
            Position(result, result)
        } catch (ex: Exception) {
            ex.printStackTrace()
            Position.ZERO
        }
    }
) {
    fun toMulticall(user: String): MultiCallElement {
        return MultiCallElement(
            function(user), address
        )
    }
}