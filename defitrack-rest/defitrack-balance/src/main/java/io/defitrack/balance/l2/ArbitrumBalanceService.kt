package io.defitrack.balance.l2

import io.defitrack.balance.BalanceService
import io.defitrack.balance.TokenBalance
import io.defitrack.common.network.Network
import io.defitrack.ethereum.config.ArbitrumContractAccessor
import io.defitrack.ethereum.config.ArbitrumGateway
import io.defitrack.token.ERC20Resource
import org.springframework.stereotype.Service
import org.web3j.protocol.core.DefaultBlockParameterName
import java.math.BigDecimal
import java.math.BigInteger
import java.math.RoundingMode

@Service
class ArbitrumBalanceService(
    private val arbitrumGateway: ArbitrumGateway,
    private val arbitrumContractAccessor: ArbitrumContractAccessor,
    private val erc20Resource: ERC20Resource,
) : BalanceService {


    override fun getNetwork(): Network = Network.ARBITRUM

    override fun getNativeBalance(address: String): BigDecimal =
        arbitrumGateway.web3j().ethGetBalance(address, DefaultBlockParameterName.LATEST).send().balance
            .toBigDecimal().divide(
                BigDecimal.TEN.pow(18), 4, RoundingMode.HALF_UP
            )

    override fun getTokenBalances(user: String): List<TokenBalance> {
        return try {
            val tokenAddresses = erc20Resource.getAllTokens(getNetwork()).map {
                it.address
            }

            return erc20Resource.getBalancesFor(user, tokenAddresses, arbitrumContractAccessor)
                .mapIndexed { i, balance ->

                    if (balance > BigInteger.ZERO) {
                        val token = erc20Resource.getERC20(getNetwork(), tokenAddresses[i])
                        TokenBalance(
                            address = token.address,
                            amount = balance,
                            decimals = token.decimals,
                            symbol = token.symbol,
                            name = token.name,
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

    override fun nativeTokenName(): String {
        return "ETH"
    }
}