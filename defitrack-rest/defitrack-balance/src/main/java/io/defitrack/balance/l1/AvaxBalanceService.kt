package io.defitrack.balance.l1

import io.defitrack.avalanche.config.AvalancheContractAccessor
import io.defitrack.avalanche.config.AvalancheGateway
import io.defitrack.balance.BalanceService
import io.defitrack.balance.TokenBalance
import io.defitrack.common.network.Network
import io.defitrack.common.utils.BigDecimalExtensions.dividePrecisely
import io.defitrack.token.ERC20Resource
import org.springframework.stereotype.Service
import org.web3j.protocol.core.DefaultBlockParameterName
import java.math.BigDecimal
import java.math.BigInteger

@Service
class AvaxBalanceService(
    private val avalancheGateway: AvalancheGateway,
    private val avalancheContractAccessor: AvalancheContractAccessor,
    private val erC20Service: ERC20Resource
) : BalanceService {

    override fun getNetwork(): Network = Network.AVALANCHE

    override fun getNativeBalance(address: String) =
        avalancheGateway.web3j().ethGetBalance(address, DefaultBlockParameterName.LATEST).send().balance
            .toBigDecimal().dividePrecisely(BigDecimal.TEN.pow(18))

    override fun getTokenBalances(user: String): List<TokenBalance> {
        val tokenAddresses = erC20Service.getAllTokens(getNetwork()).map {
            it.address
        }

        if (tokenAddresses.isEmpty()) {
            return emptyList()
        }

        return erC20Service.getBalancesFor(user, tokenAddresses, avalancheContractAccessor)
            .mapIndexed { i, balance ->
                if (balance > BigInteger.ZERO) {
                    val token = erC20Service.getTokenInformation(getNetwork(), tokenAddresses[i])
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
        return "AVAX"
    }
}