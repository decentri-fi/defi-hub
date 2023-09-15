package io.defitrack.protocol.contract

import io.defitrack.abi.TypeUtils
import io.defitrack.abi.TypeUtils.Companion.toUint256
import io.defitrack.abi.TypeUtils.Companion.uint256
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.ERC20Contract
import kotlinx.coroutines.Deferred
import java.math.BigInteger

class VelodromeV1GaugeContract(
    blockchainGateway: BlockchainGateway, address: String
) : ERC20Contract(
    blockchainGateway, address
) {

    val stakedToken: Deferred<String> = constant("stake", TypeUtils.address())
    val getRewardListLength: Deferred<BigInteger> = constant("rewardsListLength", uint256())

    suspend fun getRewardList(): List<String> {
        return readMultiCall((0 until getRewardListLength.await().toInt()).map {
            createFunction(
                "rewards",
                listOf(it.toBigInteger().toUint256()),
                listOf(TypeUtils.address())
            )
        }).mapNotNull {
            if (it.success) it.data else null
        }.map {
            it[0].value as String
        }
    }
}