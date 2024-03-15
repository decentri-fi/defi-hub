package io.defitrack.protocol.idex

import io.defitrack.abi.TypeUtils.Companion.address
import io.defitrack.abi.TypeUtils.Companion.toAddress
import io.defitrack.abi.TypeUtils.Companion.toUint256
import io.defitrack.abi.TypeUtils.Companion.uint256
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.ContractCall
import io.defitrack.evm.contract.DeprecatedEvmContract
import java.math.BigInteger

class IdexFarmContract(
    blockchainGateway: BlockchainGateway, address: String
) : DeprecatedEvmContract(blockchainGateway, address) {


    fun userInfoFunction(poolId: Int): (String) -> ContractCall {
        return { user ->
            createFunction(
                "userInfo",
                listOf(
                    poolId.toBigInteger().toUint256(),
                    user.toAddress()
                ),
                listOf(
                    uint256(),
                    uint256(),
                )
            )
        }
    }


    suspend fun poolLength(): BigInteger {
        return readSingle("poolLength", uint256())
    }

    suspend fun getLpTokenForPoolId(poolIndex: Int): String {
        return read(
            "poolInfo",
            inputs = listOf(poolIndex.toBigInteger().toUint256()),
            outputs = listOf(
                address(),
                uint256(),
                uint256(),
                uint256()
            )
        )[0].value as String
    }

    suspend fun rewardToken(): String {
        return readSingle("rewardToken", address())
    }
}