package io.defitrack.protocol.maple.contract

import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.BlockchainGateway.Companion.toAddress
import io.defitrack.evm.contract.BlockchainGateway.Companion.toUint16
import io.defitrack.evm.contract.BlockchainGateway.Companion.toUint256
import io.defitrack.evm.contract.BlockchainGateway.Companion.uint256
import io.defitrack.evm.contract.EvmContract
import org.web3j.abi.datatypes.Function
import org.web3j.abi.datatypes.generated.Uint256
import java.math.BigInteger

class LendingPoolContract(blockchainGateway: BlockchainGateway, abi: String, address: String) :
    EvmContract(
        blockchainGateway, abi, address
    ) {

    fun depositFunction(user: String, asset: String, amount: BigInteger): Function {
        return createFunction(
            "deposit",
            listOf(
                asset.toAddress(),
                amount.toUint256(),
                "0000000000000000000000000000000000000000".toAddress(),
                BigInteger.ZERO.toUint16()
            ),
            emptyList()
        )
    }

    fun getUserAccountDataFunction(user: String): Function {
        return createFunction(
            method = "getUserAccountData",
            inputs = listOf(
                user.toAddress()
            ),
            outputs = listOf(
                uint256(),
                uint256(),
                uint256(),
                uint256(),
                uint256(),
                uint256(),
                uint256()
            )
        )
    }

    fun getUserAccountData(user: String): UserAccountData {
        val retVal = read(
            method = "getUserAccountData",
            inputs = listOf(
                user.toAddress()
            ),
            outputs = listOf(
                org.web3j.abi.TypeReference.create(Uint256::class.java),
                org.web3j.abi.TypeReference.create(Uint256::class.java),
                org.web3j.abi.TypeReference.create(Uint256::class.java),
                org.web3j.abi.TypeReference.create(Uint256::class.java),
                org.web3j.abi.TypeReference.create(Uint256::class.java),
                org.web3j.abi.TypeReference.create(Uint256::class.java),
            )
        )
        return with(retVal) {
            UserAccountData(
                this[0].value as string,
                this[1].value as string,
                this[2].value as string,
                this[3].value as string,
                this[4].value as string,
                this[5].value as string,
                this[6].value as string
            )
        }
    }
}
