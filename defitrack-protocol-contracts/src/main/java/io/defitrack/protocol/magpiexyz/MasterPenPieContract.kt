package io.defitrack.protocol.magpiexyz

import arrow.core.nel
import io.defitrack.abi.TypeUtils.Companion.address
import io.defitrack.abi.TypeUtils.Companion.bool
import io.defitrack.abi.TypeUtils.Companion.toAddress
import io.defitrack.abi.TypeUtils.Companion.toUint256
import io.defitrack.abi.TypeUtils.Companion.uint256
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.ContractCall
import io.defitrack.evm.contract.DeprecatedEvmContract
import org.web3j.abi.datatypes.DynamicArray
import java.math.BigInteger

class MasterPenPieContract(
    blockchainGateway: BlockchainGateway, address: String
) : DeprecatedEvmContract(
    blockchainGateway, address
) {

    val poolLength = constant<BigInteger>("poolLength", uint256())

    suspend fun registeredToken(poolNumber: BigInteger): String {
        return readSingle("registeredToken", listOf(poolNumber.toUint256()), address())
    }

    fun userInfo(stakedToken: String): (String) -> ContractCall {
        return { user ->
            createFunction(
                "userInfo",
                listOf(stakedToken.toAddress(), user.toAddress()),
                uint256().nel()
            )
        }
    }

    fun multiclaimFn(stakingTokens: List<String>): ContractCall {
        return createFunction(
            "multiclaim",
            DynamicArray(stakingTokens.map { it.toAddress() }).nel(),
            emptyList()
        )
    }

    suspend fun tokenToPoolInfo(token: String): PoolInfo {
        val response = read(
            "tokenToPoolInfo", listOf(token.toAddress()), listOf(
                address(),
                address(),
                uint256(),
                uint256(),
                uint256(),
                uint256(),
                address(),
                bool()
            )
        )

        return PoolInfo(
            response[0].value as String,
            response[1].value as String,
            response[5].value as BigInteger,
            MasterPenPieRewarder(blockchainGateway, response[6].value as String),
            response[7].value as Boolean
        )
    }

    data class PoolInfo(
        val stakingToken: String,
        val receiptToken: String,
        val totalStaked: BigInteger,
        val rewarder: MasterPenPieRewarder,
        val active: Boolean
    )

    /*
    (bonusTokenAddresses, bonusTokenSymbols) = IBaseRewardPool(
                pool.rewarder
            ).rewardTokenInfos();
     */

    /**
     * rewarder: function earned(address account, address token)
     *         external
     *         view
     *         returns (uint256);
     */

    class MasterPenPieRewarder(blockchainGateway: BlockchainGateway, address: String) :
        DeprecatedEvmContract(blockchainGateway, address) {

        fun earnedFn(token: String): (String) -> ContractCall {
            return {
                createFunction("earned", listOf(it.toAddress(), token.toAddress()), uint256().nel())
            }
        }

        val rewardLength = constant<BigInteger>("getRewardLength", uint256())

        suspend fun rewardTokens(): List<String> {
            val result = readMultiCall(
                (0 until rewardLength.await().toInt()).map { index ->
                    createFunction(
                        "rewardTokens",
                        index.toUint256().nel(),
                        address().nel()
                    )
                }
            )

            return result.filter {
                it.success
            }.map {
                it.data.first().value as String
            }
        }
    }
}