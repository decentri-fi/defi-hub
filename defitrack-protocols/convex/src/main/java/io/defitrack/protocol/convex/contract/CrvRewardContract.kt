package io.defitrack.protocol.convex.contract

import io.defitrack.evm.contract.EvmContract
import io.defitrack.evm.contract.BlockchainGateway
import org.web3j.abi.TypeReference
import org.web3j.abi.datatypes.generated.Uint256
import java.math.BigInteger

class CrvRewardContract(
    solidityBasedContractAccessor: BlockchainGateway,
    abi: String,
    address: String
) : EvmContract(solidityBasedContractAccessor, abi, address) {


    fun earned(address: String): BigInteger {
        return readWithAbi(
            "earned",
            outputs = listOf(TypeReference.create(Uint256::class.java))
        )[0].value as BigInteger
    }
}