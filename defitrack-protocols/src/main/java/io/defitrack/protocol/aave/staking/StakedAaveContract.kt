package io.defitrack.protocol.aave.staking

import io.defitrack.abi.TypeUtils.Companion.toAddress
import io.defitrack.abi.TypeUtils.Companion.uint256
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.BlockchainGateway.Companion.MAX_UINT256
import io.defitrack.evm.contract.ContractCall
import io.defitrack.evm.contract.ERC20Contract
import io.defitrack.evm.position.Position
import org.web3j.abi.datatypes.Type
import java.math.BigDecimal
import java.math.BigInteger

class StakedAaveContract(
    blockchainGateway: BlockchainGateway,
    address: String
) : ERC20Contract(
    blockchainGateway,
    address
) {

    fun extractBalanceFunction(ratioProvider: suspend () -> BigDecimal): suspend (List<Type<*>>) -> Position =
        { retVal ->
            val userStAave = (retVal[0].value as BigInteger)

            if (userStAave > BigInteger.ZERO) {
                Position(
                    userStAave.toBigDecimal().times(ratioProvider.invoke()).toBigInteger(),
                    userStAave
                )
            } else Position.ZERO
        }

    fun getClaimRewardsFunction(user: String): ContractCall {
        return createFunction(
            "claimRewards",
            inputs = listOf(
                user.toAddress(),
                MAX_UINT256
            )
        )
    }

    fun getTotalRewardFunction(user: String): ContractCall {
        return createFunction(
            method = "getTotalRewardsBalance",
            inputs = listOf(user.toAddress()),
            outputs = listOf(
                uint256()
            )
        )
    }
}