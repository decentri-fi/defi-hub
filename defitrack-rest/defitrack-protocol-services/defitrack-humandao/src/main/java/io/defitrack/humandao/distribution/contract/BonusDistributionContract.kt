package io.defitrack.humandao.distribution.contract

import io.defitrack.evm.contract.EvmContract
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.abi.TypeUtils.Companion.toUint256
import org.web3j.abi.TypeReference
import org.web3j.abi.datatypes.Bool
import java.math.BigInteger

class BonusDistributionContract(
    blockchainGateway: BlockchainGateway, abi: String, address: String
) : EvmContract(blockchainGateway, abi, address) {

    fun isClaimed(index: Long): Boolean {
        return readWithAbi(
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