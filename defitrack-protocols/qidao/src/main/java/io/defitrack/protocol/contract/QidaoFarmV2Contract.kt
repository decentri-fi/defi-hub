package io.defitrack.protocol.contract

import io.defitrack.abi.TypeUtils
import io.defitrack.abi.TypeUtils.Companion.address
import io.defitrack.abi.TypeUtils.Companion.toAddress
import io.defitrack.abi.TypeUtils.Companion.toUint256
import io.defitrack.abi.TypeUtils.Companion.uint16
import io.defitrack.abi.TypeUtils.Companion.uint256
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.EvmContract
import org.web3j.abi.TypeReference
import org.web3j.abi.datatypes.Address
import org.web3j.abi.datatypes.Function
import org.web3j.abi.datatypes.generated.Uint16
import org.web3j.abi.datatypes.generated.Uint256
import java.math.BigInteger

class QidaoFarmV2Contract(

    contractAccessor: BlockchainGateway,
    address: String,
) : EvmContract(
    contractAccessor, address
) {

    suspend fun poolLength(): Int {
        return (read(
            "poolLength",
            outputs = listOf(uint256())
        )[0].value as BigInteger).toInt()
    }

    fun getPending(user: String): Function {
        return createFunction(
            "pending",
            inputs = listOf(user.toAddress()),
            outputs = listOf(uint256())
        )
    }

    suspend fun rewardToken(): String {
        return readSingle("erc20", address())
    }

    suspend fun getLpTokenForPoolId(poolIndex: Int): String {
        return read(
            "poolInfo",
            inputs = listOf(poolIndex.toBigInteger().toUint256()),
            outputs = listOf(
                address(),
                uint256(),
                uint256(),
                uint256(),
                uint16()
            )
        )[0].value as String
    }

    fun userInfoFunction(address: String, poolIndex: Int): Function {
        return createFunction(
            "userInfo",
            inputs = listOf(poolIndex.toBigInteger().toUint256(), address.toAddress()),
            outputs = listOf(
                uint256(),
                uint256(),
            )
        )
    }

}