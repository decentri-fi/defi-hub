package io.defitrack.protocol.mstable.contract

import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.abi.TypeUtils.Companion.toAddress
import io.defitrack.evm.contract.ERC20Contract
import org.web3j.abi.TypeReference
import org.web3j.abi.datatypes.Function
import org.web3j.abi.datatypes.generated.Uint256
import java.math.BigInteger

class MStableEthereumBoostedSavingsVaultContract(
    ethereumContractAccessor: BlockchainGateway,
    abi: String,
    address: String,
) : ERC20Contract(ethereumContractAccessor, abi, address) {


    fun rawBalanceOfFunction(address: String): Function {
        return createFunctionWithAbi(
            "rawBalanceOf",
            inputs = listOf(address.toAddress()),
            outputs = listOf(
                TypeReference.create(Uint256::class.java)
            )
        )
    }

    fun unclaimedRewards(address: String): BigInteger {
        return readWithAbi(
            "unclaimedRewards",
            inputs = listOf(address.toAddress()),
            outputs = listOf(
                TypeReference.create(Uint256::class.java)
            )
        )[0].value as BigInteger
    }

    val rewardsToken by lazy {
        (readWithAbi(
            "rewardsToken"
        )[0].value as String)
    }

    val stakingToken by lazy {
        (readWithAbi(
            "stakingToken"
        )[0].value as String)
    }
}