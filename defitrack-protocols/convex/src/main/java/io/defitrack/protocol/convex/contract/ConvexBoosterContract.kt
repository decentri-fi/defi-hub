package io.defitrack.protocol.convex.contract

import io.defitrack.evm.contract.EvmContract
import io.defitrack.evm.contract.EvmContractAccessor
import io.defitrack.evm.contract.EvmContractAccessor.Companion.toUint256
import io.defitrack.evm.contract.multicall.MultiCallElement
import org.web3j.abi.TypeReference
import org.web3j.abi.datatypes.Address
import org.web3j.abi.datatypes.Bool
import org.web3j.abi.datatypes.generated.Uint256
import java.math.BigInteger

class ConvexBoosterContract(
    evmContractAccessor: EvmContractAccessor,
    abi: String,
    address: String
) : EvmContract(evmContractAccessor, abi, address) {

    val poolLength by lazy {
        (read(
            "poolLength",
            outputs = listOf(TypeReference.create(Uint256::class.java))
        )[0].value as BigInteger).toInt()
    }

    val poolInfos: List<PoolInfo> by lazy {
        val multicalls = (0 until poolLength).map { poolIndex ->
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

        val results = this.evmContractAccessor.readMultiCall(
            multicalls
        )
        results.map { retVal ->
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

}

class PoolInfo(
    val lpToken: String,
    val token: String,
    val gauge: String,
    val crvRewards: String,
    val stash: String,
    val shutDown: Boolean
)