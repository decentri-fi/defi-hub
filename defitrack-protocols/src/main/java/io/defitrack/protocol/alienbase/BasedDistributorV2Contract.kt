package io.defitrack.protocol.alienbase

import io.defitrack.abi.TypeUtils
import io.defitrack.abi.TypeUtils.Companion.dynamicArray
import io.defitrack.abi.TypeUtils.Companion.toAddress
import io.defitrack.abi.TypeUtils.Companion.toUint256
import io.defitrack.abi.TypeUtils.Companion.uint16
import io.defitrack.abi.TypeUtils.Companion.uint256
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.EvmContract
import org.web3j.abi.datatypes.Address
import org.web3j.abi.datatypes.Function
import org.web3j.abi.datatypes.Utf8String
import org.web3j.abi.datatypes.generated.Uint256
import java.math.BigInteger

class BasedDistributorV2Contract(
    blockchainGateway: BlockchainGateway,
    contractAddress: String
) : EvmContract(
    blockchainGateway, contractAddress
) {


    suspend fun poolLength(): Int {
        return readSingle<BigInteger>("poolLength", uint256()).toInt()
    }


    suspend fun poolRewarders(poolId: Int): List<String> {
        return (read(
            "poolRewarders",
            listOf(poolId.toBigInteger().toUint256()),
            listOf(dynamicArray<Address>())
        )[0].value as List<Address>).map { it.value as String }
    }

    suspend fun alb(): String {
        return readSingle("alb", TypeUtils.address())
    }

    suspend fun poolInfos(): List<PoolInfo> {
        val functions = (0 until poolLength()).map { poolIndex ->
            createFunction(
                "poolInfo",
                inputs = listOf(poolIndex.toBigInteger().toUint256()),
                outputs = listOf(
                    TypeUtils.address(),
                    uint256(),
                    uint256(),
                    uint256(),
                    uint16(),
                    uint256(),
                    uint256(),
                )
            )
        }
        val results = readMultiCall(functions)
        return results.map { retVal ->
            PoolInfo(
                retVal.data[0].value as String,
                retVal.data[1].value as BigInteger,
                retVal.data[2].value as BigInteger,
                retVal.data[3].value as BigInteger,
                retVal.data[4].value as BigInteger,
                retVal.data[5].value as BigInteger,
                retVal.data[6].value as BigInteger,
            )
        }
    }

    fun claimFunction(poolid: Int): Function {
        return createFunction(
            method = "deposit",
            inputs = listOf(poolid.toBigInteger().toUint256(), BigInteger.ZERO.toUint256()),
        )
    }

    fun pendingFunction(poolId: Int, user: String): Function {
        return createFunction(
            method = "pendingTokens",
            inputs = listOf(poolId.toBigInteger().toUint256(), user.toAddress()),
            outputs = listOf(
                dynamicArray<Address>(),
                dynamicArray<Utf8String>(),
                dynamicArray<Uint256>(),
                dynamicArray<Uint256>(),
            )
        )
    }

    fun userInfoFunction(poolIndex: Int): (String) -> Function {
        return { user ->
            createFunction(
                method = "userInfo",
                inputs = listOf(poolIndex.toBigInteger().toUint256(), user.toAddress()),
                outputs = listOf(
                    uint256(),
                    uint256(),
                    uint256(),
                    uint256(),
                )
            )
        }
    }
}