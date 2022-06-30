package io.defitrack.protocol.quickswap

import io.defitrack.abi.TypeUtils.Companion.toAddress
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.ERC20Contract
import org.web3j.abi.TypeReference
import org.web3j.abi.datatypes.Address
import org.web3j.abi.datatypes.generated.Uint256
import java.math.BigInteger

class QuickswapRewardPoolContract(
    solidityBasedContractAccessor: BlockchainGateway,
    abi: String,
    address: String,
) : ERC20Contract(
    solidityBasedContractAccessor,
    abi, address
) {

    suspend fun stakingTokenAddress(): String {
        return readWithAbi(
            method = "stakingToken",
            outputs = listOf(TypeReference.create(Address::class.java))
        )[0].value as String
    }

    suspend fun rewardsTokenAddress(): String {
        return read("rewardsToken")
    }

    suspend fun rewardRate(): BigInteger{
        return read("rewardRate")
    }
}