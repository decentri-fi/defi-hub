package io.defitrack.protocol.umami

import arrow.core.nel
import io.defitrack.abi.TypeUtils.Companion.toAddress
import io.defitrack.abi.TypeUtils.Companion.uint256
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.ContractCall
import io.defitrack.evm.contract.ERC20Contract
import java.math.BigInteger

context (BlockchainGateway)
class MarinatedUmamiContract(address: String) : ERC20Contract(this, address) {

    //TODO: create provider

    //0x2adabd6e8ce3e82f52d9998a7f64a90d294a92a4

    //balanceof is correct balance of

    //rewardTokens, loop with multicall, failure is invalid token (currently 2 enabled)
    //rewardTokens: (address) -> (uint256)


    fun getAvailableTokenRewards(user: String, token: String): ContractCall {
        return createFunction(
            "getAvailableTokenRewards",
            listOf(user.toAddress(), token.toAddress()),
            uint256().nel()
        )
    }

    suspend fun totalStaked(): BigInteger {
        return readSingle(
            "totalStaked",
            uint256()
        )
    }

    fun claimRewardsFn(): ContractCall {
        return createFunction("claimRewards")
    }
}