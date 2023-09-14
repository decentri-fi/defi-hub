package io.defitrack.protocol.aave.staking

import io.defitrack.abi.TypeUtils.Companion.toAddress
import io.defitrack.abi.TypeUtils.Companion.uint256
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.EvmContract
import org.web3j.abi.datatypes.Function
import java.math.BigInteger
import io.defitrack.evm.contract.BlockchainGateway.Companion.MAX_UINT256

class StakedAaveContract(
    blockchainGateway: BlockchainGateway,
    address: String
) : EvmContract(
    blockchainGateway,
    "", address
) {

    suspend fun totalSupply(): BigInteger {
        return read(
            "totalSupply",
            emptyList(),
            listOf(uint256())
        )[0].value as BigInteger
    }


    fun getClaimRewardsFunction(user: String): Function {
        return createFunction(
            "claimRewards",
            inputs = listOf(
                user.toAddress(),
                MAX_UINT256
            )
        )
    }
    fun getTotalRewardFunction(user: String): Function {
        return createFunction(
            "getTotalRewardsBalance",
            inputs = listOf(
                user.toAddress()
            ),
            outputs = listOf(
                uint256()
            )
        )
    }
}