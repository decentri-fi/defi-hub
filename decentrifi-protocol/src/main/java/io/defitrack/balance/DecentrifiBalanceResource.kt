package io.defitrack.balance

import io.defitrack.common.network.Network
import io.defitrack.erc20.domain.FungibleTokenInformation
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.math.BigInteger

@Component
class DecentrifiBalanceResource(
    private val client: HttpClient,
    @Value("\${balanceResourceLocation:http://defitrack-balance.default.svc.cluster.local:8080}") private val balanceResourceLocation: String
) : BalanceResource {

    override suspend fun getNativeBalance(network: Network, user: String): BalanceElement {
        return withContext(Dispatchers.IO) {
            val get = client.get("$balanceResourceLocation/$user/native-balance?network=$network")
            if (!get.status.isSuccess()) {
                BalanceElement(0.0)
            } else {
                get.body<BalanceElement>()
            }
        }
    }

    class BalanceElement(
        val amount: Double
    )
}