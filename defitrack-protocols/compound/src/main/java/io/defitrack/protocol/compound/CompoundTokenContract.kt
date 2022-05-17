package io.defitrack.protocol.compound

import io.defitrack.common.utils.BigDecimalExtensions.dividePrecisely
import io.defitrack.evm.contract.EvmContract
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.BlockchainGateway.Companion.toAddress
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