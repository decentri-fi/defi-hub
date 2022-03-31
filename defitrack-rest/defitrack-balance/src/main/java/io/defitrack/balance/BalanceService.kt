package io.defitrack.balance

import io.defitrack.common.network.Network
import io.defitrack.evm.contract.ContractAccessorGateway
import io.defitrack.token.ERC20Resource
import java.math.BigDecimal
import java.math.BigInteger

abstract class BalanceService(
    val contractAccessorGateway: ContractAccessorGateway,
    val erc20Resource: ERC20Resource
) {
    abstract fun getNetwork(): Network
    abstract fun nativeTokenName(): String

    fun getNativeBalance(address: String): BigDecimal =
        contractAccessorGateway.getGateway(getNetwork()).getNativeBalance(address)

    fun getTokenBalances(user: String): List<TokenBalance> {
        return try {
            val tokenAddresses = erc20Resource.getAllTokens(getNetwork()).map {
                it.address
            }

            return erc20Resource.getBalancesFor(user, tokenAddresses, contractAccessorGateway.getGateway(getNetwork()))
                .mapIndexed { i, balance ->

                    if (balance > BigInteger.ZERO) {
                        val token = erc20Resource.getTokenInformation(getNetwork(), tokenAddresses[i])
                        TokenBalance(
                            amount = balance,
                            token = token.toFungibleToken(),
                            network = getNetwork(),
                        )
                    } else {
                        null
                    }
                }.filterNotNull()
        } catch (ex: Exception) {
            ex.printStackTrace()
            emptyList()
        }
    }
}