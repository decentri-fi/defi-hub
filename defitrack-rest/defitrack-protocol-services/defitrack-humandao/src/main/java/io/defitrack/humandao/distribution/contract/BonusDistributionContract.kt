package io.defitrack.humandao.distribution.contract

import io.defitrack.evm.contract.EvmContract
import io.defitrack.evm.contract.EvmContractAccessor
import io.defitrack.evm.contract.EvmContractAccessor.Companion.toUint256
import org.web3j.abi.TypeReference
import org.web3j.abi.datatypes.Bool
import java.math.BigInteger

class BonusDistributionContract(
    evmContractAccessor: EvmContractAccessor, abi: String, address: String
) : EvmContract(evmContractAccessor, abi, address) {

    fun isClaimed(index: Long): Boolean {
        return read(
            method = "isClaimed",
            inputs = listOf(
                BigInteger.valueOf(index).toUint256()
            ),
            outputs = listOf(
                TypeReference.create(Bool::class.java)
            )
        )[0].value as Boolean
    }
}