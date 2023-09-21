package io.defitrack.ens.service

import io.defitrack.abi.TypeUtils.Companion.string
import io.defitrack.abi.TypeUtils.Companion.toUint256
import io.defitrack.abi.TypeUtils.Companion.uint256
import io.defitrack.common.network.Network
import io.defitrack.common.utils.AsyncUtils.lazyAsync
import io.defitrack.ens.contract.EnsRegistrarContract
import io.defitrack.ens.contract.EnsRegistryContract
import io.defitrack.ens.contract.EnsResolverContract
import io.defitrack.evm.contract.BlockchainGatewayProvider
import io.github.reactivecircus.cache4k.Cache
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.web3j.abi.datatypes.generated.Bytes32
import org.web3j.crypto.Hash
import org.web3j.ens.NameHash
import org.web3j.utils.Numeric
import java.math.BigInteger
import kotlin.time.Duration.Companion.hours

private const val ENS_REGISTRAR = "0x57f1887a8bf19b14fc0df6fd9b2acc9af147ea85"
private const val ENS_REGISTRY = "0x00000000000C2E074eC69A0dFb2997BA6C7d2e1e"

@Service
class EnsNameService(
    accessorGateway: BlockchainGatewayProvider,
) {

    private val logger = LoggerFactory.getLogger(this::class.java)

    val ethereumProvider = accessorGateway.getGateway(Network.ETHEREUM)
    val cache = Cache.Builder<String, String>().expireAfterWrite(24.hours).build()

    val ensRegistryContract = lazyAsync {
        EnsRegistryContract(ethereumProvider, ENS_REGISTRY)
    }

    val ensRegistrarContract = lazyAsync {
        EnsRegistrarContract(ethereumProvider, ENS_REGISTRAR)
    }

    suspend fun getAvatar(name: String): String {
        return cache.get("${name}:avatar") {
            val resolver = getResolverContract(name)
            resolver.getText(name, "avatar")
        }
    }

    suspend fun getEnsByName(name: String): String {
        return cache.get(name) {
            val resolver = getResolverContract(name)
            resolver.getAddress(name)
        }
    }

    val expiresCache = Cache.Builder<String, BigInteger>().expireAfterWrite(1.hours).build()

    suspend fun getExpires(ensName: String): BigInteger {
        return expiresCache.get(ensName) {
            ensRegistrarContract.await().getExpires(ensName)
        }
    }

    suspend fun getEnsByAddress(address: String): String {
        return cache.get("reverse-$address") {
            try {
                val reverseName = Numeric.cleanHexPrefix(address) + ".addr.reverse"
                val resolver = getResolverContract(reverseName)
                resolver.getName(reverseName)
            } catch (ex: Exception) {
                logger.debug("Unable to fetch ens for address $address")
                ""
            }
        }
    }


    val resolverCache = Cache.Builder<String, EnsResolverContract>().build()

    private suspend fun getResolverContract(name: String): EnsResolverContract {
        return resolverCache.get(name) {
            val resolverAddress = ensRegistryContract.await().getResolver(name)
            EnsResolverContract(
                ethereumProvider,
                resolverAddress
            )
        }
    }
}