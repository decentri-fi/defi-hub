package io.defitrack.protocol.compound

import io.defitrack.abi.TypeUtils.Companion.toUint256
import io.defitrack.abi.TypeUtils.Companion.uint256
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.ERC20Contract
import org.web3j.abi.datatypes.Function
import java.math.BigInteger

class IronbankTokenContract(
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
            outputs = listOf(uint256())
        )[0].value as BigInteger
    }

    suspend fun supplyRatePerBlock(): BigInteger {
        return readWithAbi(
            "supplyRatePerBlock",
            outputs = listOf(uint256())
        )[0].value as BigInteger
    }
}