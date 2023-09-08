package io.defitrack.ens.service

import io.defitrack.abi.TypeUtils.Companion.address
import io.defitrack.abi.TypeUtils.Companion.string
import io.defitrack.abi.TypeUtils.Companion.toUint256
import io.defitrack.abi.TypeUtils.Companion.toUtf8String
import io.defitrack.abi.TypeUtils.Companion.uint256
import io.defitrack.common.network.Network
import io.defitrack.evm.contract.BlockchainGatewayProvider
import io.github.reactivecircus.cache4k.Cache
import org.springframework.stereotype.Service
import org.web3j.abi.datatypes.generated.Bytes32
import org.web3j.crypto.Hash
import org.web3j.ens.NameHash
import org.web3j.utils.Numeric
import java.math.BigInteger
import kotlin.time.Duration.Companion.hours

@Service
class EnsNameService(
    accessorGateway: BlockchainGatewayProvider,
) {

    val ethereumProvider = accessorGateway.getGateway(Network.ETHEREUM)

    val cache = Cache.Builder<String, String>().expireAfterWrite(24.hours).build()

    suspend fun getAvatar(name: String): String {
        return cache.get("${name}:avatar") {
            val resolver = getResolver(name)

            ethereumProvider.readFunction(
                resolver,
                "text",
                outputs = listOf(string()),
                inputs = listOf(Bytes32(NameHash.nameHashAsBytes(name)), "avatar".toUtf8String())
            )[0].value as String
        }
    }

    suspend fun getEnsByName(name: String): String {
        return cache.get(name) {
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

    suspend fun getExpires(label: String): BigInteger {
        val splitted = label.split(".")
        val sha = Hash.sha3String(splitted[splitted.size - 2])
        val tokenId = BigInteger(sha.removePrefix("0x"), 16)

        return ethereumProvider.readFunction(
            "0x57f1887a8bf19b14fc0df6fd9b2acc9af147ea85",
            "nameExpires",
            listOf(tokenId.toUint256()),
            listOf(uint256())
        )[0].value as BigInteger
    }

    suspend fun getEnsByAddress(address: String): String {
        return cache.get("reverse-$address") {
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