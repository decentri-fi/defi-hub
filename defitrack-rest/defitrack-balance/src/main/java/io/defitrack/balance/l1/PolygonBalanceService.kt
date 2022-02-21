package io.defitrack.balance.l1

import io.defitrack.balance.BalanceService
import io.defitrack.balance.TokenBalance
import io.defitrack.common.network.Network
import io.defitrack.polygon.config.PolygonContractAccessor
import io.defitrack.polygon.config.PolygonGateway
import io.defitrack.token.ERC20Resource
import org.springframework.stereotype.Service
import org.web3j.protocol.core.DefaultBlockParameterName
import java.math.BigDecimal
import java.math.BigInteger
import java.math.RoundingMode

@Service
class PolygonBalanceService(
    private val maticGateway: PolygonGateway,
    private val polygonContractAccessor: PolygonContractAccessor,
    private val erC20Service: ERC20Resource
) : BalanceService {

    override fun getNetwork(): Network = Network.POLYGON

    override fun getNativeBalance(address: String): BigDecimal =
        maticGateway.web3j().ethGetBalance(address, DefaultBlockParameterName.LATEST).send().balance
            .toBigDecimal().divide(
                BigDecimal.TEN.pow(18), 4, RoundingMode.HALF_UP
            )

    override fun getTokenBalances(user: String): List<TokenBalance> {
        val tokenAddresses = erC20Service.getAllTokens(getNetwork()).map {
            it.address
        }

        if (tokenAddresses.isEmpty()) {
            return emptyList()
        }

        return erC20Service.getBalancesFor(user, tokenAddresses, polygonContractAccessor)
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
        return "MATIC"
    }
}