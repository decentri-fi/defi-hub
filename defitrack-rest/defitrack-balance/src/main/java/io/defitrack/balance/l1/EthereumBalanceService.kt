package io.defitrack.balance.l1

import io.defitrack.balance.BalanceService
import io.defitrack.balance.TokenBalance
import io.defitrack.common.network.Network
import io.defitrack.ethereum.config.EthereumContractAccessor
import io.defitrack.ethereum.config.EthereumGateway
import io.defitrack.token.ERC20Resource
import org.springframework.stereotype.Service
import org.web3j.protocol.core.DefaultBlockParameterName
import java.math.BigDecimal
import java.math.BigInteger
import java.math.RoundingMode

@Service
class EthereumBalanceService(
    private val ethereumContractAccessor: EthereumContractAccessor,
    private val ethereumGateway: EthereumGateway,
    private val erc20Resource: ERC20Resource,
) : BalanceService {
    override fun getNetwork(): Network = Network.ETHEREUM

    override fun getNativeBalance(address: String): BigDecimal =
        ethereumGateway.web3j().ethGetBalance(address, DefaultBlockParameterName.LATEST).send().balance
            .toBigDecimal().divide(
                BigDecimal.TEN.pow(18), 4, RoundingMode.HALF_UP
            )

    override fun getTokenBalances(user: String): List<TokenBalance> {
        val tokenAddresses = erc20Resource.getAllTokens(getNetwork()).map {
            it.address
        }

        if (tokenAddresses.isEmpty()) {
            return emptyList()
        }

        return erc20Resource.getBalancesFor(user, tokenAddresses, ethereumContractAccessor)
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
    }

    override fun nativeTokenName(): String {
        return "ETH"
    }
}