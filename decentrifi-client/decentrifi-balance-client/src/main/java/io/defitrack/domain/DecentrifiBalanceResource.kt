package io.defitrack.domain

import io.defitrack.common.network.Network
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class DecentrifiBalanceResource(
    private val client: HttpClient,
    @Value("\${balanceResourceLocation:http://defitrack-balance.default.svc.cluster.local:8080}") private val balanceResourceLocation: String
) : BalanceResource {

    override suspend fun getNativeBalance(network: Network, user: String): BalanceElement {
        return withContext(Dispatchers.IO) {
            val get = client.get("$balanceResourceLocation/$user/native-balance?network=$network")
            if (!get.status.isSuccess()) {
                throw IllegalArgumentException("Unable to get balance")
            } else {
                get.body<BalanceElement>()
            }
        }
    }

    override suspend fun getNativeBalances(user: String): List<BalanceElement> {
        return withContext(Dispatchers.IO) {
            val get = client.get("$balanceResourceLocation/$user/native-balance")
            if (!get.status.isSuccess()) {
                emptyList()
            } else {
                get.body<List<BalanceElement>>()
            }
        }
    }

    override suspend fun getTokenBalance(network: Network, user: String, token: String): BalanceElement {
        return withContext(Dispatchers.IO) {
            val get = client.get("$balanceResourceLocation/$user/$token?network=$network")
            if (!get.status.isSuccess()) {
                throw IllegalArgumentException("unable to fetch balance")
            } else {
                get.body<BalanceElement>()
            }
        }
    }


}