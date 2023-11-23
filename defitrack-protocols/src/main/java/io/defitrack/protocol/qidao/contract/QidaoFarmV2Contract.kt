package io.defitrack.protocol.qidao.contract

import io.defitrack.abi.TypeUtils.Companion.address
import io.defitrack.abi.TypeUtils.Companion.toAddress
import io.defitrack.abi.TypeUtils.Companion.toUint256
import io.defitrack.abi.TypeUtils.Companion.uint16
import io.defitrack.abi.TypeUtils.Companion.uint256
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.ContractCall
import io.defitrack.evm.contract.EvmContract
import org.web3j.abi.datatypes.Function
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

    fun getPending(user: String): ContractCall {
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

    fun userInfoFunction(poolIndex: Int): (String) -> ContractCall {
        return { user ->
            createFunction(
                "userInfo",
                inputs = listOf(poolIndex.toBigInteger().toUint256(), user.toAddress()),
                outputs = listOf(
                    uint256(),
                    uint256(),
                )
            )
        }
    }

}