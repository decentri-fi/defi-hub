package io.defitrack.protocol.compound

import io.defitrack.common.utils.BigDecimalExtensions.dividePrecisely
import io.defitrack.evm.contract.EvmContract
import io.defitrack.evm.contract.EvmContractAccessor
import io.defitrack.evm.contract.EvmContractAccessor.Companion.toAddress
import org.web3j.abi.TypeReference
import org.web3j.abi.datatypes.Function
import org.web3j.abi.datatypes.generated.Uint256
import java.math.BigDecimal
import java.math.BigInteger

class CompoundTokenContract(
    ethereumContractAccessor: EvmContractAccessor,
    abi: String,
    address: String,
) : EvmContract(
    ethereumContractAccessor, abi, address
) {

    val symbol: String by lazy {
        read(
            "symbol"
        )[0].value as String
    }


    val name: String by lazy {
        read(
            "name"
        )[0].value as String
    }

    val cash: BigInteger by lazy {
        read(
            "getCash"
        )[0].value as BigInteger
    }

    val totalBorrows: BigInteger by lazy {
        read(
            "totalBorrows"
        )[0].value as BigInteger
    }


    val underlyingAddress: String? by lazy {
        try {
            read(
                "underlying"
            )[0].value as String
        } catch (ex: Exception) {
            "0xc02aaa39b223fe8d0a0e5c4f27ead9083c756cc2"
        }
    }

    val decimals: BigInteger by lazy {
        try {
            read(
                "decimals"
            )[0].value as BigInteger
        } catch (ex: Exception) {
            BigInteger.valueOf(18)
        }
    }

    fun balanceOf(address: String): BigInteger {
        return read(
            "balanceOf",
            inputs = listOf(address.toAddress()),
            outputs = listOf(
                TypeReference.create(Uint256::class.java)
            )
        )[0].value as BigInteger
    }

    fun underlyingBalanceOf(address: String): BigInteger {
        return balanceOf(address).times(exchangeRate).toBigDecimal().dividePrecisely(BigDecimal.TEN.pow(18)).toBigInteger()
    }

    val exchangeRate by lazy {
        read(
            "exchangeRateStored",
            outputs = listOf(
                TypeReference.create(Uint256::class.java)
            )
        )[0].value as BigInteger
    }

    fun borrowBalanceStoredFunction(address: String): Function {
        return createFunction(
            "borrowBalanceStored",
            inputs = listOf(address.toAddress()),
            outputs = listOf(TypeReference.create(Uint256::class.java))
        )
    }

    fun borrowBalanceStored(address: String): BigInteger {
        return read(
            "borrowBalanceStored",
            inputs = listOf(address.toAddress()),
            outputs = listOf(TypeReference.create(Uint256::class.java))
        )[0].value as BigInteger
    }

    val supplyRatePerBlock by lazy {
        read(
            "supplyRatePerBlock",
            outputs = listOf(TypeReference.create(Uint256::class.java))
        )[0].value as BigInteger
    }

    val borrowRatePerBlock by lazy {
        read(
            "borrowRatePerBlock",
            outputs = listOf(TypeReference.create(Uint256::class.java))
        )[0].value as BigInteger
    }
}