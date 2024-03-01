package io.defitrack.ens.infrastructure.adapter.out.blockchain

import io.defitrack.abi.TypeUtils
import io.defitrack.abi.TypeUtils.Companion.address
import io.defitrack.abi.TypeUtils.Companion.toUtf8String
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.EvmContract
import org.web3j.abi.datatypes.generated.Bytes32
import org.web3j.ens.NameHash

context(BlockchainGateway)
class EnsResolverContract(
    address: String
) : EvmContract(address) {

    suspend fun getText(ensName: String, textName: String): String {
        return readSingle(
            "text",
            listOf(Bytes32(NameHash.nameHashAsBytes(ensName)), textName.toUtf8String()),
            TypeUtils.string()
        )
    }

    suspend fun getAddress(ensName: String): String {
        return readSingle(
            "addr",
            listOf(Bytes32(NameHash.nameHashAsBytes(ensName))),
            address(),
        )
    }

    suspend fun getName(name: String): String {
        val result: String = readSingle(
            "name",
            listOf(Bytes32(NameHash.nameHashAsBytes(name))),
            TypeUtils.string(),
        )

        if (result == "0x0000000000000000000000000000000000000000") {
            return ""
        }
        return result
    }
}