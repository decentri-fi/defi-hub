package io.defitrack.protocol.polygon

import io.defitrack.abi.TypeUtils.Companion.address
import io.defitrack.abi.TypeUtils.Companion.toAddress
import io.defitrack.abi.TypeUtils.Companion.toUint256
import io.defitrack.abi.TypeUtils.Companion.uint256
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.ContractCall
import io.defitrack.evm.contract.EvmContract
import java.math.BigInteger

class PolygonStakingContract(
    blockchainGateway: BlockchainGateway, address: String
) : EvmContract(
    blockchainGateway, address
) {

    suspend fun nftCounter(): BigInteger {
        return readSingle("NFTCounter", uint256())
    }

    suspend fun getStakingShareProviders(): List<String> {
        return readMultiCall(
            (0 until nftCounter().toInt()).map {
                createFunction(
                    "getValidatorContract",
                    listOf(it.toBigInteger().toUint256()),
                    listOf(address())
                )
            }
        ).filter { it.success }
            .map { it.data[0].value as String }
            .filter {
                it != "0x0000000000000000000000000000000000000000"
            }
    }


    fun totalStakedForFn(user: String): ContractCall {
        return createFunction(
            "totalStakedFor",
            inputs = listOf(user.toAddress()),
            outputs = listOf(uint256())
        )
    }

    suspend fun token(): String {
        return readSingle("token", address())
    }
}