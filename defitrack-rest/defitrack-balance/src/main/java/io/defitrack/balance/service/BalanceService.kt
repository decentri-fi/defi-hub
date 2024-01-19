package io.defitrack.balance.service

import io.defitrack.balance.service.dto.TokenBalance
import io.defitrack.common.network.Network
import io.defitrack.evm.contract.BlockchainGatewayProvider
import io.defitrack.evm.contract.ContractCall
import io.defitrack.evm.contract.ERC20Contract
import io.defitrack.port.input.ERC20Resource
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

    suspend fun getNativeBalance(address: String): BigDecimal {
        return try {
            return blockchainGatewayProvider.getGateway(getNetwork()).getNativeBalance(address)
        } catch (ex: Exception) {
            logger.error(ex.message)
            BigDecimal.ZERO
        }
    }

    open suspend fun getTokenBalances(user: String): List<TokenBalance> {
        return try {
            val tokenAddresses = erc20Resource.getAllTokens(getNetwork(), verified = true).map {
                it.address
            }

            return getBalancesFor(user, tokenAddresses, getNetwork())
                .mapIndexed { i, balance ->
                    if (balance > BigInteger.ZERO) {
                        val token = erc20Resource.getTokenInformation(getNetwork(), tokenAddresses[i])
                        TokenBalance(
                            amount = balance,
                            token = token,
                            network = getNetwork(),
                        )
                    } else {
                        null
                    }
                }.filterNotNull()
        } catch (ex: Exception) {
            logger.error("Unable to get token balances for {}", this.getNetwork())
            emptyList()
        }
    }


    private suspend fun getBalancesFor(
        address: String,
        tokens: List<String>,
        network: Network,
    ): List<BigInteger> {
        with(blockchainGatewayProvider.getGateway(network)) {
            return readMultiCall(tokens.map { token ->
                ContractCall(
                    ERC20Contract.balanceOf(address),
                    network,
                    token
                )
            }).map {
                try {
                    if (!it.success) {
                        BigInteger.ZERO
                    } else {
                        it.data[0].value as BigInteger
                    }
                } catch (_: Exception) {
                    BigInteger.ZERO
                }
            }
        }
    }
}