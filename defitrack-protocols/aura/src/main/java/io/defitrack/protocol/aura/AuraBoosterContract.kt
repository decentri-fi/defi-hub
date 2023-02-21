package io.defitrack.protocol.aura

import io.defitrack.abi.TypeUtils.Companion.toUint256
import io.defitrack.abi.TypeUtils.Companion.uint256
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.EvmContract
import io.defitrack.evm.contract.multicall.MultiCallElement
import org.web3j.abi.TypeReference
import org.web3j.abi.datatypes.Address
import org.web3j.abi.datatypes.Bool
import org.web3j.abi.datatypes.generated.Uint256
import java.math.BigInteger

class AuraBoosterContract(
    blockchainGateway: BlockchainGateway
) : EvmContract(
    blockchainGateway, "", "0xa57b8d98dae62b26ec3bcc4a365338157060b234"
) {

    suspend fun poolLength(): Int {
        return (readWithoutAbi(
            "poolLength",
            outputs = listOf(uint256())
        )[0].value as BigInteger).toInt()
    }


    suspend fun poolInfos(): List<PoolInfo> {
        val multicalls = (0 until poolLength()).map { poolIndex ->
            MultiCallElement(
                createFunction(
                    "poolInfo",
                    inputs = listOf(poolIndex.toBigInteger().toUint256()),
                    outputs = listOf(
                        TypeReference.create(Address::class.java),
                        TypeReference.create(Address::class.java),
                        TypeReference.create(Address::class.java),
                        TypeReference.create(Address::class.java),
                        TypeReference.create(Address::class.java),
                        TypeReference.create(Bool::class.java),
                    )
                ),
                this.address
            )
        }

        val results = this.blockchainGateway.readMultiCall(
            multicalls
        )
        return results.map { retVal ->
            PoolInfo(
                retVal[0].value as String,
                retVal[1].value as String,
                retVal[2].value as String,
                retVal[3].value as String,
                retVal[4].value as String,
                retVal[5].value as Boolean,
            )
        }
    }

    data class PoolInfo(
        val lpToken: String,
        val token: String,
        val gauge: String,
        val crvRewards: String,
        val stash: String,
        val isStaking: Boolean,
    )
}