package io.defitrack.protocol.dinoswap.contract

import io.defitrack.abi.TypeUtils.Companion.address
import io.defitrack.abi.TypeUtils.Companion.toAddress
import io.defitrack.abi.TypeUtils.Companion.toUint256
import io.defitrack.abi.TypeUtils.Companion.uint256
import io.defitrack.common.utils.AsyncUtils.lazyAsync
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.protocol.sushiswap.contract.MasterChefBasedContract
import kotlinx.coroutines.Deferred
import org.web3j.abi.datatypes.Function

class DinoswapFossilFarmsContract(
    blockchainGateway: BlockchainGateway,
    address: String,
) : MasterChefBasedContract(
    "DINO",
    "pendingDino",
    blockchainGateway = blockchainGateway,
    address
) {

    val lpTokens: Deferred<List<String>> = lazyAsync {
        readMultiCall((0 until poolLength.await().toInt()).map { poolIndex ->
            createFunction(
                "poolInfo",
                listOf(poolIndex.toBigInteger().toUint256()),
                listOf(
                    address(),
                    uint256(),
                    uint256(),
                    uint256(),
                )
            )
        }).map {
            it.data[0].value as String
        }
    }


    suspend fun lpTokenForPoolId(poolIndex: Int): String {
        return lpTokens.await()[poolIndex]
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