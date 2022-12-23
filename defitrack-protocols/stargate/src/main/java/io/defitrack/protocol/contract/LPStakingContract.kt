package io.defitrack.protocol.contract

import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.abi.TypeUtils.Companion.toUint256
import io.defitrack.evm.contract.EvmContract
import io.defitrack.evm.contract.multicall.MultiCallElement
import org.web3j.abi.TypeReference
import org.web3j.abi.datatypes.Address
import org.web3j.abi.datatypes.generated.Uint256
import java.math.BigInteger

class LPStakingContract(
    blockchainGateway: BlockchainGateway,
    abi: String, address: String
) : EvmContract(
    blockchainGateway, abi, address
) {

    val stargate by lazy {
        readWithAbi("stargate")[0].value as String
    }

    fun lpBalances(index: Int): BigInteger {
        return readWithAbi(
            "lpBalances",
            inputs = listOf(index.toBigInteger().toUint256()),
            outputs = listOf(
                TypeReference.create(Uint256::class.java)
            )
        )[0].value as BigInteger
    }

    val poolInfos by lazy {

        val multicalls = (0 until poolLength).map { poolIndex ->
            MultiCallElement(
                createFunctionWithAbi(
                    "poolInfo",
                    inputs = listOf(poolIndex.toBigInteger().toUint256()),
                    outputs = listOf(
                        TypeReference.create(Address::class.java),
                        TypeReference.create(Uint256::class.java),
                        TypeReference.create(Uint256::class.java),
                        TypeReference.create(Uint256::class.java)
                    )
                ),
                this.address
            )
        }

        val results = this.blockchainGateway.readMultiCall(
            multicalls
        )
        results.map { retVal ->
            PoolInfo(
                retVal[0].value as String,
                retVal[1].value as BigInteger,
                retVal[2].value as BigInteger,
                retVal[3].value as BigInteger,
            )
        }
    }

    class PoolInfo(
        val lpToken: String,
        val allocPoint: BigInteger,
        val lastRewardBlock: BigInteger,
        val accStargatePerShare: BigInteger
    )


    val poolLength by lazy {
        (readWithAbi(
            "poolLength",
            outputs = listOf(TypeReference.create(Uint256::class.java))
        )[0].value as BigInteger).toInt()
    }

    val totalAllocPoint by lazy {
        readWithAbi(
            "totalAllocPoint",
            outputs = listOf(TypeReference.create(Uint256::class.java))
        )[0].value as BigInteger
    }

}