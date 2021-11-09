package io.defitrack.crv

import io.defitrack.ethereumbased.contract.SolidityBasedContractAccessor.Companion.toAddress
import io.defitrack.ethereumbased.contract.SolidityBasedContractAccessor.Companion.toUint256
import io.defitrack.ethereumbased.contract.SolidityContract
import io.defitrack.matic.config.PolygonContractAccessor
import org.web3j.abi.TypeReference
import org.web3j.abi.datatypes.Address
import org.web3j.abi.datatypes.generated.Uint256
import java.math.BigInteger

class CrvPolygonRegistryContract(
    ethereumContractAccessor: PolygonContractAccessor,
    abi: String,
    address: String,
) : SolidityContract(
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