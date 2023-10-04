package io.defitrack.protocol.qidao.contract

import io.defitrack.abi.TypeUtils.Companion.address
import io.defitrack.abi.TypeUtils.Companion.bool
import io.defitrack.abi.TypeUtils.Companion.toUint256
import io.defitrack.abi.TypeUtils.Companion.uint256
import io.defitrack.common.utils.AsyncUtils.lazyAsync
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.ERC721Contract
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import java.math.BigInteger

class QidaoVaultContract(
    blockchainGateway: BlockchainGateway,
    address: String
) : ERC721Contract(
    blockchainGateway, address
) {

    suspend fun vaultCount(): BigInteger {
        return readSingle("vaultCount", uint256())
    }

    suspend fun exists(vaultId: BigInteger): Boolean {
        return try {
            read(
                method = "exists",
                inputs = listOf(vaultId.toUint256()),
                outputs = listOf(bool())
            )[0].value as Boolean
        } catch (ex: Exception) {
            false
        }
    }

    suspend fun vaultDebt(vaultId: BigInteger): BigInteger {
        return read(
            "vaultDebt",
            inputs = listOf(vaultId.toUint256()),
            outputs = listOf(uint256())
        )[0].value as BigInteger
    }

    suspend fun vaultCollateral(vaultId: BigInteger): BigInteger {
        return read(
            "vaultCollateral",
            inputs = listOf(vaultId.toUint256()),
            outputs = listOf(uint256())
        )[0].value as BigInteger
    }

    suspend fun collateral(): String {
        return read(
            "collateral",
            outputs = listOf(address())
        )[0].value as String
    }

    suspend fun getVaults(user: String): List<Int> {
        return readMultiCall(
            existingVaults.await().map {
                ownerOfFunction(it.toBigInteger())
            }
        ).mapIndexed { index, value ->
            if ((value.data[0].value as String).lowercase() == user.lowercase()) {
                return@mapIndexed index
            } else {
                return@mapIndexed null
            }
        }.filterNotNull()
    }

    private val existingVaults = lazyAsync {
        (0 until vaultCount().toInt()).map {
            async {
                exists(it.toBigInteger())
            }
        }.awaitAll().mapIndexed { index, value ->
            if (value) {
                return@mapIndexed index
            } else {
                return@mapIndexed null
            }
        }.filterNotNull()
    }

    suspend fun populateVaultOwners() {
        logger.info("populated ${existingVaults.await().count()} owners")
    }
}