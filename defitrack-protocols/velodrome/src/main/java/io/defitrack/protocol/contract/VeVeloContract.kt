package io.defitrack.protocol.contract

import io.defitrack.abi.TypeUtils
import io.defitrack.abi.TypeUtils.Companion.toAddress
import io.defitrack.abi.TypeUtils.Companion.toUint256
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.ERC20Contract
import io.defitrack.evm.contract.multicall.MultiCallElement
import org.web3j.abi.datatypes.Function
import java.math.BigInteger

class VeVeloContract(
    blockchainGateway: BlockchainGateway, address: String
) : ERC20Contract(
    blockchainGateway, "", address
) {

    suspend fun getTokenIdsForOwner(owner: String): List<BigInteger> {
        val balance = balanceOf(owner)
        return blockchainGateway.readMultiCall(
            (0 until balance.toInt()).map { index ->
                MultiCallElement(
                    createFunction(
                        "tokenOfOwnerByIndex",
                        inputs = listOf(owner.toAddress(), index.toBigInteger().toUint256()),
                        outputs = listOf(
                            TypeUtils.uint256(),
                        )
                    ),
                    address
                )
            }).map {
            it[0].value as BigInteger
        }
    }

    fun createBalanceOfNFTFn(tokenIndex: BigInteger): Function {
        return createFunction(
            "balanceOfNFT",
            listOf(tokenIndex.toUint256()),
            listOf(TypeUtils.uint256())
        )
    }
}