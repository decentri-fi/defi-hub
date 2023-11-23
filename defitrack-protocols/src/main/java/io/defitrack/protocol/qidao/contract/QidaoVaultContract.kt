package io.defitrack.protocol.qidao.contract

import io.defitrack.abi.TypeUtils.Companion.address
import io.defitrack.abi.TypeUtils.Companion.bool
import io.defitrack.abi.TypeUtils.Companion.toAddress
import io.defitrack.abi.TypeUtils.Companion.toUint256
import io.defitrack.abi.TypeUtils.Companion.uint256
import io.defitrack.common.utils.AsyncUtils.lazyAsync
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.ContractCall
import io.defitrack.evm.contract.ERC721Contract
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import org.web3j.abi.datatypes.Function
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


    val collateral = constant<String>("collateral", address())

    suspend fun getVaults(user: String): List<Int> {
        return readMultiCall(
            (0 until balanceOf(user).toInt()).map {
                tokenOfOwnerByIndex(user, it)
            }
        ).filter {
            it.success
        }.map { (it.data[0].value as BigInteger) }
            .map(BigInteger::toInt)
    }

    fun tokenOfOwnerByIndex(user: String, index: Int): ContractCall {
        return createFunction(
            "tokenOfOwnerByIndex",
            listOf(
                user.toAddress(),
                index.toBigInteger().toUint256()
            ),
            listOf(uint256())
        )
    }
}