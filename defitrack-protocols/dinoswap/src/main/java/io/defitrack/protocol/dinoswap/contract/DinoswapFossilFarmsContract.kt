package io.defitrack.protocol.dinoswap.contract

import io.defitrack.abi.TypeUtils.Companion.address
import io.defitrack.abi.TypeUtils.Companion.toAddress
import io.defitrack.abi.TypeUtils.Companion.toUint256
import io.defitrack.abi.TypeUtils.Companion.uint256
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.EvmContract
import org.web3j.abi.datatypes.Function
import java.math.BigInteger

class DinoswapFossilFarmsContract(
    contractAccessor: BlockchainGateway,
    abi: String,
    address: String,
) : EvmContract(
    contractAccessor, abi, address
) {

    suspend fun poolLength(): Int {
        return (readWithAbi(
            "poolLength",
            outputs = listOf(uint256())
        )[0].value as BigInteger).toInt()
    }

    suspend fun rewardToken(): String {
        return readWithAbi(
            "dino",
            outputs = listOf(uint256())
        )[0].value as String
    }

    suspend fun getLpTokenForPoolId(poolIndex: Int): String {
        return readWithAbi(
            "poolInfo",
            inputs = listOf(poolIndex.toBigInteger().toUint256()),
            outputs = listOf(
                address(),
                uint256(),
                uint256(),
                uint256(),
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