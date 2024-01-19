package io.defitrack.ens.infrastructure.adapter.out.blockchain

import io.defitrack.abi.TypeUtils
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.EvmContract
import org.web3j.abi.datatypes.generated.Bytes32
import org.web3j.ens.NameHash

class EnsRegistryContract(
    blockchainGateway: BlockchainGateway,
    address: String
) : EvmContract(blockchainGateway, address) {

    suspend fun getResolver(name: String): String {
        val nameHash = NameHash.nameHashAsBytes(name)
        return read(
            "resolver",
            outputs = listOf(TypeUtils.address()),
            inputs = listOf(Bytes32(nameHash))
        )[0].value as String
    }
}