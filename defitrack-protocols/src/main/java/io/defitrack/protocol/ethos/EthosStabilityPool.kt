package io.defitrack.protocol.ethos

import io.defitrack.abi.TypeUtils.Companion.address
import io.defitrack.abi.TypeUtils.Companion.toAddress
import io.defitrack.abi.TypeUtils.Companion.toUint256
import io.defitrack.abi.TypeUtils.Companion.uint256
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.EvmContract
import org.web3j.abi.datatypes.Function
import java.math.BigInteger

class EthosStabilityPool(
    blockchainGateway: BlockchainGateway, address: String
) : EvmContract(
    blockchainGateway, address
) {

    val ernToken = constant<String>("lusdToken", address())

    fun depositsFn(user: String): Function {
        return createFunction(
            "getCompoundedLUSDDeposit",
            listOf(user.toAddress()),
            listOf(uint256())
        )
    }

    fun claimableFn(user: String): Function {
        return createFunction(
            "getDepositorLQTYGain",
            listOf(user.toAddress()),
            listOf(uint256())
        )
    }

    fun claimFn(user: String): ContractCall {
        return createFunction(
            "withdrawFromSP",
            listOf(BigInteger.ZERO.toUint256())
        ).toContractCall()
    }

}