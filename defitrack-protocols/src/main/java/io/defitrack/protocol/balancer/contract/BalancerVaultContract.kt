package io.defitrack.protocol.balancer.contract

import io.defitrack.abi.TypeUtils.Companion.toBytes32
import io.defitrack.abi.TypeUtils.Companion.uint256
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.EvmContract
import org.bouncycastle.util.encoders.Hex
import org.web3j.abi.TypeReference
import org.web3j.abi.datatypes.Address
import org.web3j.abi.datatypes.DynamicArray
import org.web3j.abi.datatypes.generated.Uint256
import java.math.BigInteger

class BalancerVaultContract(
    blockchainGateway: BlockchainGateway,
    address: String
) : EvmContract(
    blockchainGateway, address
) {

    suspend fun getPoolTokens(poolId: String): PoolTokenResult {
        val bytes = Hex.decode(poolId.removePrefix("0x"))
        val result = read(
            method = "getPoolTokens",
            inputs = listOf(bytes.toBytes32()),
            outputs = listOf(
                object : TypeReference<DynamicArray<Address>>() {},
                object : TypeReference<DynamicArray<Uint256>>() {},
                uint256()
            )
        )

        val tokens = (result[0].value as List<Address>).map { it.value as String }
        val balances = (result[1].value as List<Uint256>).map { it.value as BigInteger }

        return PoolTokenResult(tokens, balances)
    }

    data class PoolTokenResult(
        val tokens: List<String>,
        val balances: List<BigInteger>
    )
}