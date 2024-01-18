package io.defitrack.ens.application

import io.defitrack.common.network.Network
import io.defitrack.common.utils.AsyncUtils
import io.defitrack.ens.contract.EnsRegistrarContract
import io.defitrack.ens.contract.EnsRegistryContract
import io.defitrack.ens.contract.EnsResolverContract
import io.defitrack.ens.port.input.ENSUseCase
import io.defitrack.evm.contract.BlockchainGatewayProvider
import io.github.reactivecircus.cache4k.Cache
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.web3j.utils.Numeric
import java.math.BigInteger
import kotlin.time.Duration.Companion.hours

private const val ENS_REGISTRAR = "0x57f1887a8bf19b14fc0df6fd9b2acc9af147ea85"
private const val ENS_REGISTRY = "0x00000000000C2E074eC69A0dFb2997BA6C7d2e1e"


@Component
class EnsNameService(
    accessorGateway: BlockchainGatewayProvider,
) : ENSUseCase {

    private val logger = LoggerFactory.getLogger(this::class.java)

    val ethereumProvider = accessorGateway.getGateway(Network.ETHEREUM)
    val cache = Cache.Builder<String, String>().expireAfterWrite(24.hours).build()

    val ensRegistryContract = AsyncUtils.lazyAsync {
        EnsRegistryContract(ethereumProvider, ENS_REGISTRY)
    }

    val ensRegistrarContract = AsyncUtils.lazyAsync {
        EnsRegistrarContract(ethereumProvider, ENS_REGISTRAR)
    }

    override suspend fun getAvatar(name: String): String {
        return cache.get("${name}:avatar") {
            val resolver = getResolverContract(name)
            resolver.getText(name, "avatar")
        }
    }

    override suspend fun getEnsByName(name: String): String {
        return cache.get(name) {
            try {
                val resolver = getResolverContract(name)
                resolver.getAddress(name)
            } catch (ex: Exception) {
                ""
            }
        }
    }

    val expiresCache = Cache.Builder<String, BigInteger>().expireAfterWrite(1.hours).build()

    override suspend fun getExpires(ensName: String): BigInteger {
        if (ensName == "0x0000000000000000000000000000000000000000") {
            return BigInteger.ZERO
        }
        return try {
            expiresCache.get(ensName) {
                ensRegistrarContract.await().getExpires(ensName)
            }
        } catch (ex: Exception) {
            logger.error("Unable to fetch expires for $ensName")
            BigInteger.ZERO
        }
    }

    override suspend fun getEnsByAddress(address: String): String {
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