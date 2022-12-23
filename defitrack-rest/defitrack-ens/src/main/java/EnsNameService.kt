package io.defitrack

import io.defitrack.abi.TypeUtils.Companion.address
import io.defitrack.abi.TypeUtils.Companion.string
import io.defitrack.common.network.Network
import io.defitrack.evm.contract.BlockchainGatewayProvider
import kotlinx.coroutines.runBlocking
import org.springframework.stereotype.Service
import org.web3j.abi.datatypes.generated.Bytes32
import org.web3j.ens.NameHash
import org.web3j.utils.Numeric

@Service
class EnsNameService(
    accessorGateway: BlockchainGatewayProvider,
) {

    val ethereumProvider = accessorGateway.getGateway(Network.ETHEREUM)

    fun getEnsByName(name: String) = runBlocking {
        return@runBlocking ethereumProvider.readFunction(
            "0x4976fb03c32e5b8cfe2b6ccb31c09ba78ebaba41",
            "addr",
            outputs = listOf(address()),
            inputs = listOf(Bytes32(NameHash.nameHashAsBytes(name)))
        )[0].value as String
    }

    fun getEnsByAddress(address: String) = runBlocking {
        val nameHashAsBytes = NameHash.nameHashAsBytes("${Numeric.cleanHexPrefix(address)}.addr.reverse")
        return@runBlocking ethereumProvider.readFunction(
            "0x4976fb03c32e5b8cfe2b6ccb31c09ba78ebaba41",
            "name",
            outputs = listOf(string()),
            inputs = listOf(Bytes32(nameHashAsBytes))
        )[0].value as String
    }
}