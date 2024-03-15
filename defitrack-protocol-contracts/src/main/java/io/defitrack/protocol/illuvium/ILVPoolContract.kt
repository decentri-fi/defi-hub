package io.defitrack.protocol.illuvium

import arrow.core.nel
import io.defitrack.abi.TypeUtils
import io.defitrack.abi.TypeUtils.Companion.toAddress
import io.defitrack.abi.TypeUtils.Companion.toUint256
import io.defitrack.abi.TypeUtils.Companion.uint256
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.ContractCall
import io.defitrack.evm.contract.DeprecatedEvmContract
import java.math.BigInteger

context (BlockchainGateway)
class ILVPoolContract(address: String) : DeprecatedEvmContract(this, address) {

    /*
     Pool addresses:
     0xe98477bdc16126bb0877c6e3882e3edd72571cc2
     0x7f5f854ffb6b7701540a00c69c4ab2de2b34291d
     */


    val poolToken = constant<String>("poolToken", TypeUtils.address())

    fun claimVaultRewaredsFn(): ContractCall {
        return createFunction(
            "claimVaultRewards",
        )
    }

    suspend fun getStakesLength(user: String) {
        return readSingle(
            "getStakesLength",
            user.toAddress().nel(),
            uint256()
        )
    }

    suspend fun getStake(user: String, stakedId: BigInteger): Stake {
        val result = read(
            "getStake",
            listOf(user.toAddress(), stakedId.toUint256()),
            listOf(
                //TODO: TypeReference.create(Stake::class),
            )
        )
        return Stake(BigInteger.ZERO)
    }


    suspend fun pendingRewards(user: String) {
        return readSingle(
            "pendingRewards",
            user.toAddress().nel(),
            uint256()
        )
    }

    //struct, so use dynamicstruct
    /*
    struct Data {
        /// @dev token amount staked
        uint120 value;
        /// @dev locking period - from
        uint64 lockedFrom;
        /// @dev locking period - until
        uint64 lockedUntil;
        /// @dev indicates if the stake was created as a yield reward
        bool isYield;
    }
     */
    data class Stake(
        val value: BigInteger,
        //...
    )


}