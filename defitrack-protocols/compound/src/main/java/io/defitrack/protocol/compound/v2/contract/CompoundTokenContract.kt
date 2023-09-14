package io.defitrack.protocol.compound.v2.contract

import io.defitrack.abi.TypeUtils
import io.defitrack.abi.TypeUtils.Companion.toAddress
import io.defitrack.abi.TypeUtils.Companion.toUint256
import io.defitrack.abi.TypeUtils.Companion.uint256
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.ERC20Contract
import org.web3j.abi.datatypes.Function
import java.math.BigInteger

class CompoundTokenContract(
    ethereumContractAccessor: BlockchainGateway,
    address: String,
) : ERC20Contract(
    ethereumContractAccessor, address
) {

    fun mintFunction(amount: BigInteger): Function {
        return createFunction(
            "mint",
            listOf(amount.toUint256()),
            emptyList()
        )
    }

    suspend fun cash(): BigInteger {
        return readSingle("getCash", uint256())
    }

    suspend fun totalBorrows(): BigInteger {
        return readSingle("totalBorrows", uint256())
    }

    suspend fun underlyingAddress(): String {
        return try {
            readSingle<String>(
                "underlying", TypeUtils.address()
            )
        } catch (ex: Exception) {
            "0xc02aaa39b223fe8d0a0e5c4f27ead9083c756cc2"
        }
    }

    suspend fun exchangeRate(): BigInteger {
        return readSingle("exchangeRateStored", uint256())
    }

    fun borrowBalanceStoredFunction(address: String): Function {
        return createFunction(
            "borrowBalanceStored",
            inputs = listOf(address.toAddress()),
            outputs = listOf(uint256())
        )
    }

    suspend fun supplyRatePerBlock(): BigInteger {
        return readSingle("supplyRatePerBlock", uint256())
    }

    suspend fun borrowRatePerBlock(): BigInteger {
        return readSingle("borrowRatePerBlock", uint256())
    }
}