package io.defitrack.protocol.reward

import io.defitrack.ethereumbased.contract.EvmContract
import io.defitrack.ethereumbased.contract.EvmContractAccessor
import io.defitrack.ethereumbased.contract.EvmContractAccessor.Companion.toUint256
import org.web3j.abi.TypeReference
import org.web3j.abi.datatypes.generated.Uint128
import org.web3j.abi.datatypes.generated.Uint64
import java.math.BigInteger

class MiniChefV2Contract(
    private val evmContractAccessor: EvmContractAccessor,
    abi: String, address: String
) : EvmContract(evmContractAccessor, abi, address) {


    fun accSushiPerShare(poolIndex: Int): BigInteger {
        return read(
            "poolInfo",
            inputs = listOf(poolIndex.toBigInteger().toUint256()),
            outputs = listOf(
                TypeReference.create(Uint128::class.java),
                TypeReference.create(Uint64::class.java),
                TypeReference.create(Uint64::class.java),
            )
        )[0].value as BigInteger
    }

}