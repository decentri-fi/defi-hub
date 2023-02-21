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
        val nameHash = NameHash.nameHashAsBytes(name)
        val resolver = ethereumProvider.readFunction(
            "0x00000000000C2E074eC69A0dFb2997BA6C7d2e1e",
            "resolver",
            outputs = listOf(address()),
            inputs = listOf(Bytes32(nameHash))
        )[0].value as String

        return@runBlocking ethereumProvider.readFunction(
            resolver,
            "addr",
            outputs = listOf(address()),
            inputs = listOf(Bytes32(NameHash.nameHashAsBytes(name)))
        )[0].value as String
    }

    fun getEnsByAddress(address: String) = runBlocking {
        val reverseName = Numeric.cleanHexPrefix(address) + ".addr.reverse"
        val nameHash = NameHash.nameHashAsBytes(reverseName)
        val resolver = ethereumProvider.readFunction(
            "0x00000000000C2E074eC69A0dFb2997BA6C7d2e1e",
            "resolver",
            outputs = listOf(address()),
            inputs = listOf(Bytes32(nameHash))
        )[0].value as String


        return@runBlocking ethereumProvider.readFunction(
            resolver,
            "name",
            outputs = listOf(string()),
            inputs = listOf(Bytes32(nameHash))
        )[0].value as String
    }
}