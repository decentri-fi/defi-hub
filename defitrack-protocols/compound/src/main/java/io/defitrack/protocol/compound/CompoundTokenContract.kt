package io.defitrack.protocol.compound

import io.defitrack.abi.TypeUtils.Companion.toAddress
import io.defitrack.abi.TypeUtils.Companion.toUint256
import io.defitrack.common.utils.BigDecimalExtensions.dividePrecisely
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.ERC20Contract
import org.web3j.abi.TypeReference
import org.web3j.abi.datatypes.Function
import org.web3j.abi.datatypes.generated.Uint256
import java.math.BigDecimal
import java.math.BigInteger

class CompoundTokenContract(
    ethereumContractAccessor: BlockchainGateway,
    abi: String,
    address: String,
) : ERC20Contract(
    ethereumContractAccessor, abi, address
) {

    fun mintFunction(amount: BigInteger): Function {
        return createFunctionWithAbi(
            "mint",
            listOf(amount.toUint256()),
            emptyList()
        )
    }

    suspend fun cash(): BigInteger {
        return readWithAbi(
            "getCash"
        )[0].value as BigInteger
    }

    suspend fun totalBorrows(): BigInteger {
        return readWithAbi(
            "totalBorrows"
        )[0].value as BigInteger
    }


    suspend fun underlyingAddress(): String {
        return try {
            readWithAbi(
                "underlying"
            )[0].value as String
        } catch (ex: Exception) {
            "0xc02aaa39b223fe8d0a0e5c4f27ead9083c756cc2"
        }
    }

    suspend fun exchangeRate(): BigInteger {
        return readWithAbi(
            "exchangeRateStored",
            outputs = listOf(
                TypeReference.create(Uint256::class.java)
            )
        )[0].value as BigInteger
    }

    fun borrowBalanceStoredFunction(address: String): Function {
        return createFunctionWithAbi(
            "borrowBalanceStored",
            inputs = listOf(address.toAddress()),
            outputs = listOf(TypeReference.create(Uint256::class.java))
        )
    }

    suspend fun supplyRatePerBlock(): BigInteger {
        return readWithAbi(
            "supplyRatePerBlock",
            outputs = listOf(TypeReference.create(Uint256::class.java))
        )[0].value as BigInteger
    }

    suspend fun borrowRatePerBlock(): BigInteger {
        return readWithAbi(
            "borrowRatePerBlock",
            outputs = listOf(TypeReference.create(Uint256::class.java))
        )[0].value as BigInteger
    }
}