package io.defitrack.protocol.contract

import io.defitrack.abi.TypeUtils.Companion.address
import io.defitrack.abi.TypeUtils.Companion.bool
import io.defitrack.abi.TypeUtils.Companion.toUint256
import io.defitrack.abi.TypeUtils.Companion.uint256
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.ERC721Contract
import io.defitrack.evm.contract.multicall.MultiCallElement
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking
import java.math.BigInteger

class QidaoVaultContract(
    blockchainGateway: BlockchainGateway,
    address: String
) : ERC721Contract(
    blockchainGateway, "", address
) {

    suspend fun vaultCount(): BigInteger {
        return readWithoutAbi(
            method = "vaultCount",
            outputs = listOf(uint256())
        )[0].value as BigInteger
    }

    suspend fun exists(vaultId: BigInteger): Boolean {
        return try {
            readWithoutAbi(
                method = "exists",
                inputs = listOf(vaultId.toUint256()),
                outputs = listOf(bool())
            )[0].value as Boolean
        } catch (ex: Exception) {
            false
        }
    }

    suspend fun vaultDebt(vaultId: BigInteger): BigInteger {
        return readWithoutAbi(
            "vaultDebt",
            inputs = listOf(vaultId.toUint256()),
            outputs = listOf(uint256())
        )[0].value as BigInteger
    }

    suspend fun vaultCollateral(vaultId: BigInteger): BigInteger {
        return readWithoutAbi(
            "vaultCollateral",
            inputs = listOf(vaultId.toUint256()),
            outputs = listOf(uint256())
        )[0].value as BigInteger
    }

    suspend fun collateral(): String {
        return readWithoutAbi(
            "collateral",
            outputs = listOf(address())
        )[0].value as String
    }

    suspend fun getVaults(user: String): List<Int> {
        return blockchainGateway.readMultiCall(
            existingVaults.map {
                ownerOfFunction(it.toBigInteger())
            }.map {
                MultiCallElement(
                    it, address
                )
            }
        ).mapIndexed { index, value ->
            if ((value[0].value as String).lowercase() == user.lowercase()) {
                return@mapIndexed index
            } else {
                return@mapIndexed null
            }
        }.filterNotNull()
    }

    private val existingVaults by lazy {
       runBlocking {
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
    }

    fun populateVaultOwners() {
        logger.info("populated ${existingVaults.count()} owners")
    }
}