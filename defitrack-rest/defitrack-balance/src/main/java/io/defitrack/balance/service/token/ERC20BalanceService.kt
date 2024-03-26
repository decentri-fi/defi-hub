package io.defitrack.balance.service.token

import arrow.core.Either.Companion.catch
import arrow.core.getOrElse
import io.defitrack.common.network.Network
import io.defitrack.evm.contract.BlockchainGatewayProvider
import io.defitrack.evm.contract.ERC20Contract
import org.springframework.stereotype.Component
import java.math.BigInteger

@Component
class ERC20BalanceService(private val blockchainGatewayProvider: BlockchainGatewayProvider) {
    suspend fun getBalance(
        network: Network,
        tokenAddress: String,
        userAddress: String
    ): BigInteger {

        val provider = blockchainGatewayProvider.getGateway(network)
        return catch {
            with(provider) { ERC20Contract(tokenAddress).balanceOf(userAddress) }
        }.mapLeft {
            it.printStackTrace()
        }.getOrElse {
            BigInteger.ZERO
        }
    }
}