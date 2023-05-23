package io.defitrack

import io.defitrack.abi.TypeUtils.Companion.address
import io.defitrack.abi.TypeUtils.Companion.string
import io.defitrack.abi.TypeUtils.Companion.toUtf8String
import io.defitrack.common.network.Network
import io.defitrack.evm.contract.BlockchainGatewayProvider
import io.github.reactivecircus.cache4k.Cache
import kotlinx.coroutines.runBlocking
import org.springframework.stereotype.Service
import org.web3j.abi.datatypes.generated.Bytes32
import org.web3j.ens.NameHash
import org.web3j.utils.Numeric
import kotlin.time.Duration.Companion.hours

@Service
class EnsNameService(
    accessorGateway: BlockchainGatewayProvider,
) {

    val ethereumProvider = accessorGateway.getGateway(Network.ETHEREUM)

    val cache = Cache.Builder<String, String>().expireAfterWrite(24.hours).build()

    fun getAvatar(name: String) = runBlocking {
        cache.get("${name}:avatar") {
            val resolver = getResolver(name)

            ethereumProvider.readFunction(
                resolver,
                "text",
                outputs = listOf(string()),
                inputs = listOf(Bytes32(NameHash.nameHashAsBytes(name)), "avatar".toUtf8String())
            )[0].value as String
        }
    }

    fun getEnsByName(name: String) = runBlocking {
        cache.get(name) {
            val resolver = getResolver(name)

            ethereumProvider.readFunction(
                resolver,
                "addr",
                outputs = listOf(address()),
                inputs = listOf(Bytes32(NameHash.nameHashAsBytes(name)))
            )[0].value as String
        }
    }

    private suspend fun getResolver(name: String): String {
        val nameHash = NameHash.nameHashAsBytes(name)
        val resolver = ethereumProvider.readFunction(
            "0x00000000000C2E074eC69A0dFb2997BA6C7d2e1e",
            "resolver",
            outputs = listOf(address()),
            inputs = listOf(Bytes32(nameHash))
        )[0].value as String
        return resolver
    }

    fun getEnsByAddress(address: String) = runBlocking {
        cache.get("reverse-$address") {
            val reverseName = Numeric.cleanHexPrefix(address) + ".addr.reverse"
            val resolver = getResolver(reverseName)

            ethereumProvider.readFunction(
                resolver,
                "name",
                outputs = listOf(string()),
                inputs = listOf(Bytes32(NameHash.nameHashAsBytes(reverseName)))
            )[0].value as String
        }
    }
}