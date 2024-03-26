package io.defitrack.erc20.application

import arrow.core.Option
import io.defitrack.common.network.Network
import io.defitrack.erc20.domain.TokenInformation
import io.defitrack.token.TokenType
import io.ktor.util.collections.*
import org.springframework.stereotype.Component

typealias Cache = ConcurrentMap<String, Option<TokenInformation>>


@Component
class TokenCache {

    val cache: Cache = Cache()

    private fun createIndex(address: String, network: Network): String {
        return "${address.lowercase()}-$network"
    }

    fun put(address: String, network: Network, tokenInformation: Option<TokenInformation>): Option<TokenInformation> {
        cache[createIndex(address, network)] = tokenInformation
        return tokenInformation
    }

    fun get(address: String, network: Network): Option<TokenInformation>? {
        return cache[createIndex(address, network)]
    }

    fun getAll(): HashMap<String, Option<TokenInformation>> {
        return HashMap(cache)
    }

    fun find(network: Network, verified: Boolean): List<TokenInformation> =
        HashMap(cache).asSequence().filter {
            it.value.isSome()
        }.filter {
            network == it.value.getOrNull()?.network
        }.mapNotNull {
            it.value.getOrNull()
        }.distinctBy {
            it.address.lowercase() + "-" + it.network.name
        }.filter {
            !verified || (it.verified == verified)
        }.filter {
            it.type == TokenType.SINGLE
        }.toList()
}