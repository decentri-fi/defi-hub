package io.defitrack.protocol

import io.defitrack.abi.TypeUtils
import io.defitrack.abi.TypeUtils.Companion.toAddress
import io.defitrack.abi.TypeUtils.Companion.toUint256
import io.defitrack.abi.TypeUtils.Companion.uint16
import io.defitrack.abi.TypeUtils.Companion.uint256
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.EvmContract
import io.defitrack.evm.contract.multicall.MultiCallElement
import org.web3j.abi.datatypes.Function
import java.math.BigInteger

class BasedDistributorV2Contract(
    blockchainGateway: BlockchainGateway,
    contractAddress: String
) : EvmContract(
    blockchainGateway, "", contractAddress
) {


    suspend fun poolLength(): Int {
        return readSingle<BigInteger>("poolLength", uint256()).toInt()
    }


    suspend fun rewardToken(): String {
        return readSingle("alb", TypeUtils.address())
    }

    suspend fun poolInfos(): List<PoolInfo> {
        val multicalls = (0 until poolLength()).map { poolIndex ->
            MultiCallElement(
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
                ),
                this.address
            )
        }
        val results = blockchainGateway.readMultiCall(
            multicalls
        )
        return results.map { retVal ->
            PoolInfo(
                retVal[0].value as String,
                retVal[1].value as BigInteger,
                retVal[2].value as BigInteger,
                retVal[3].value as BigInteger,
                retVal[4].value as BigInteger,
                retVal[5].value as BigInteger,
                retVal[6].value as BigInteger,
            )
        }
    }

    fun userInfoFunction(user: String, poolIndex: Int): Function {
        return createFunction(
            "userInfo",
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