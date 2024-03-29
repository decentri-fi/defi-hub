package io.defitrack.protocol.velodrome.contract

import io.defitrack.abi.TypeUtils.Companion.toAddress
import io.defitrack.abi.TypeUtils.Companion.toUint256
import io.defitrack.abi.TypeUtils.Companion.uint128
import io.defitrack.abi.TypeUtils.Companion.uint256
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.ContractCall
import io.defitrack.evm.contract.ERC20Contract
import java.math.BigInteger

context(BlockchainGateway)
class VeVeloContract(address: String) : ERC20Contract(address) {

    suspend fun getTokenIdsForOwner(owner: String): List<BigInteger> {
        val balance = balanceOf(owner)
        return readMultiCall(
            (0 until balance.toInt()).map { index ->
                createFunction(
                    "tokenOfOwnerByIndex",
                    inputs = listOf(owner.toAddress(), index.toBigInteger().toUint256()),
                    outputs = listOf(
                        uint256(),
                    )
                )
            }).map {
            it.data[0].value as BigInteger
        }
    }

    fun lockedFn(tokenIndex: BigInteger): ContractCall {
        return createFunction(
            "locked",
            listOf(tokenIndex.toUint256()),
            listOf(
                uint128(),
                uint256(),
            )
        )
    }
}