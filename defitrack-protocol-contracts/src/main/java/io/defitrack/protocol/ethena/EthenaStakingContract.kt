package io.defitrack.protocol.ethena

import arrow.core.nel
import io.defitrack.abi.TypeUtils.Companion.toAddress
import io.defitrack.abi.TypeUtils.Companion.uint104
import io.defitrack.abi.TypeUtils.Companion.uint152
import io.defitrack.abi.TypeUtils.Companion.uint256
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.ContractCall
import io.defitrack.evm.contract.EvmContract

context(BlockchainGateway)
class EthenaStakingContract(address: String) : EvmContract(address) {

    //contract: 0x8707f238936c12c309bfc2b9959c35828acfc512

    //TODO: create provider
    /*
        lps to stake: 0xd69cce0460f11edc6b0e7ab30889cf84f45ec308

        can fetch allowed staking tokens from

        StakeParametersUpdated(index address lpToken, index uint8 epoch, uint248 stakeLimit, uint104 cooldown)
     */

    /*
        stakedAmount uint256, coolingDownAmount uint152, cooldownStartTimestamp uint104
     */
    fun stakes(lp: String): (String) -> ContractCall {
        return { user: String ->
            createFunction(
                "stakes",
                user.toAddress().nel(),
                listOf(
                    uint256(), //staked amount
                    uint152(), //cooling down amount
                    uint104() //cooldownstartTimestamp
                )
            )
        }
    }

}