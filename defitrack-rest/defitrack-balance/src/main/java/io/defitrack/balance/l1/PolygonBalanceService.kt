package io.defitrack.balance.l1

import io.defitrack.balance.BalanceService
import io.defitrack.balance.TokenBalance
import io.defitrack.common.network.Network
import io.defitrack.common.utils.BigDecimalExtensions.dividePrecisely
import io.defitrack.evm.contract.ContractAccessorGateway
import io.defitrack.polygon.config.PolygonContractAccessorConfig
import io.defitrack.token.ERC20Resource
import io.ktor.client.*
import io.ktor.client.request.*
import kotlinx.coroutines.runBlocking
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.math.BigInteger

@Service
class PolygonBalanceService(
    private val erC20Service: ERC20Resource,
    private val httpClient: HttpClient,
    private val contractAccessorGateway: ContractAccessorGateway,
) : BalanceService {

    override fun getNetwork(): Network = Network.POLYGON

    override fun getNativeBalance(address: String): BigDecimal = runBlocking {
        httpClient.get<BigInteger>(
            "http://defitrack-polygon:8080/balances/$address"

        ).toBigDecimal().dividePrecisely(
            BigDecimal.TEN.pow(18)
        )
    }

    override fun getTokenBalances(user: String): List<TokenBalance> {
        val tokenAddresses = erC20Service.getAllTokens(getNetwork()).map {
            it.address
        }

        if (tokenAddresses.isEmpty()) {
            return emptyList()
        }

        return erC20Service.getBalancesFor(user, tokenAddresses, contractAccessorGateway.getGateway(getNetwork()))
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