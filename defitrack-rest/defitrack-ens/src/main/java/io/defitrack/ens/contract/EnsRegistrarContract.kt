package io.defitrack.ens.contract

import io.defitrack.abi.TypeUtils
import io.defitrack.abi.TypeUtils.Companion.toUint256
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.EvmContract
import org.web3j.crypto.Hash
import java.math.BigInteger

class EnsRegistrarContract(
    blockchainGateway: BlockchainGateway,
    address: String
) : EvmContract(blockchainGateway, "", address) {

    suspend fun getExpires(ensName: String): BigInteger {
        val splitted = ensName.split(".")
        val sha = Hash.sha3String(splitted[splitted.size - 2])
        val tokenId = BigInteger(sha.removePrefix("0x"), 16)
        return readWithoutAbi(
            "nameExpires",
            listOf(tokenId.toUint256()),
            listOf(TypeUtils.uint256())
        )[0].value as BigInteger
    }

}