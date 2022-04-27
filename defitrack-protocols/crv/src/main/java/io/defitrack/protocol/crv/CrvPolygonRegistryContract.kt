package io.defitrack.protocol.crv

import io.defitrack.evm.contract.EvmContract
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.BlockchainGateway.Companion.toAddress
import io.defitrack.evm.contract.BlockchainGateway.Companion.toUint256
import org.web3j.abi.TypeReference
import org.web3j.abi.datatypes.Address
import org.web3j.abi.datatypes.generated.Uint256
import java.math.BigInteger

class CrvPolygonRegistryContract(
    ethereumContractAccessor: BlockchainGateway,
    abi: String,
    address: String,
) : EvmContract(
    ethereumContractAccessor,
    abi,
    address
) {


    fun getGaugeController(): String {
        return read(
            "gauge_controller",
            outputs = listOf(
                TypeReference.create(Address::class.java)
            )
        )[0].value as String
    }

    fun getPoolCount(): Long {
        return (read(
            "pool_count",
            outputs = listOf(
                TypeReference.create(Uint256::class.java)
            )
        )[0].value as BigInteger).toLong()
    }

    fun getPool(poolId: Long): String {
        return read(
            "pool_list",
            inputs = listOf(
                BigInteger.valueOf(
                    poolId
                ).toUint256()
            ),
            outputs = listOf(
                TypeReference.create(Address::class.java)
            )
        )[0].value as String
    }

    fun getLpToken(poolAddress: String): String {
        return read(
            "get_lp_token",
            inputs = listOf(poolAddress.toAddress()),
            outputs = listOf(
                TypeReference.create(Address::class.java)
            )
        )[0].value as String
    }
}