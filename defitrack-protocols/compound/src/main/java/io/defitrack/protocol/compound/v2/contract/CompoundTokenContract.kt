package io.defitrack.protocol.compound.v2.contract

import io.defitrack.abi.TypeUtils
import io.defitrack.abi.TypeUtils.Companion.toAddress
import io.defitrack.abi.TypeUtils.Companion.toUint256
import io.defitrack.abi.TypeUtils.Companion.uint256
import io.defitrack.common.utils.AsyncUtils.lazyAsync
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.ERC20Contract
import io.defitrack.evm.contract.multicall.MultiCallResult
import kotlinx.coroutines.Deferred
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

    suspend fun totalBorrows(): BigInteger {
        return readSingle("totalBorrows", uint256())
    }

    val cash: Deferred<BigInteger> = constant("getCash", uint256())
    private val underlyingAddress: Deferred<String> = constant("underlying", TypeUtils.address())
    val exchangeRate: Deferred<BigInteger> = constant("exchangeRateStored", uint256())
    val supplyRatePerBlock: Deferred<BigInteger> = constant("supplyRatePerBlock", uint256())
    val borrowRatePerBlock: Deferred<BigInteger> = constant("borrowRatePerBlock", uint256())

    suspend fun getUnderlyingAddress(): String {
        return try {
            underlyingAddress.await()
        } catch (ex: Exception) {
            "0xc02aaa39b223fe8d0a0e5c4f27ead9083c756cc2"
        }
    }

    fun borrowBalanceStoredFunction(address: String): Function {
        return createFunction(
            "borrowBalanceStored",
            inputs = listOf(address.toAddress()),
            outputs = listOf(uint256())
        )
    }
}