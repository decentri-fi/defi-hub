package io.defitrack.balance

import io.defitrack.common.network.Network
import io.defitrack.evm.contract.BlockchainGatewayProvider
import io.defitrack.token.ERC20Resource
import org.slf4j.LoggerFactory
import java.math.BigDecimal
import java.math.BigInteger

abstract class BalanceService(
    val blockchainGatewayProvider: BlockchainGatewayProvider,
    val erc20Resource: ERC20Resource
) {

    val logger = LoggerFactory.getLogger(this::class.java)
    abstract fun getNetwork(): Network
    abstract fun nativeTokenName(): String

    fun getNativeBalance(address: String): BigDecimal {
        return try {
            return blockchainGatewayProvider.getGateway(getNetwork()).getNativeBalance(address)
        } catch (ex: Exception) {
            logger.error(ex.message)
            BigDecimal.ZERO
        }
    }

    fun getTokenBalances(user: String): List<TokenBalance> {
        return try {
            val tokenAddresses = erc20Resource.getAllTokens(getNetwork()).map {
                it.address
            }

            return erc20Resource.getBalancesFor(user, tokenAddresses, getNetwork())
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