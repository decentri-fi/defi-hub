package io.defitrack.protocol.contract

import io.defitrack.abi.TypeUtils
import io.defitrack.abi.TypeUtils.Companion.toAddress
import io.defitrack.abi.TypeUtils.Companion.toUint256
import io.defitrack.abi.TypeUtils.Companion.uint128
import io.defitrack.abi.TypeUtils.Companion.uint256
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.ERC20Contract
import org.web3j.abi.datatypes.Function
import java.math.BigInteger

class VeVeloContract(
    blockchainGateway: BlockchainGateway, address: String
) : ERC20Contract(
    blockchainGateway, "", address
) {

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

    fun lockedFn(tokenIndex: BigInteger): Function {
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