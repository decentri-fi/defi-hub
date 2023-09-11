package io.defitrack.ens.contract

import io.defitrack.abi.TypeUtils
import io.defitrack.abi.TypeUtils.Companion.toUtf8String
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.EvmContract
import org.web3j.abi.datatypes.generated.Bytes32
import org.web3j.ens.NameHash

class EnsResolverContract(
    blockchainGateway: BlockchainGateway,
    address: String
) : EvmContract(blockchainGateway, "", address) {

    suspend fun getText(ensName: String, textName: String): String {
        return readWithoutAbi(
            "text",
            outputs = listOf(TypeUtils.string()),
            inputs = listOf(Bytes32(NameHash.nameHashAsBytes(ensName)), textName.toUtf8String())
        )[0].value as String
    }

    suspend fun getAddress(ensName: String): String {
        return readWithoutAbi(
            "addr",
            outputs = listOf(TypeUtils.address()),
            inputs = listOf(Bytes32(NameHash.nameHashAsBytes(ensName)))
        )[0].value as String
    }

    suspend fun getName(name: String): String {
        return readWithoutAbi(
            "name",
            outputs = listOf(TypeUtils.string()),
            inputs = listOf(Bytes32(NameHash.nameHashAsBytes(name)))
        )[0].value as String
    }
}