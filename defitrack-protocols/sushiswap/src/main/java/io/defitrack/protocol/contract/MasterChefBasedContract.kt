package io.defitrack.protocol.contract

import io.defitrack.abi.TypeUtils.Companion.address
import io.defitrack.abi.TypeUtils.Companion.toAddress
import io.defitrack.abi.TypeUtils.Companion.toUint256
import io.defitrack.abi.TypeUtils.Companion.uint256
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.EvmContract
import io.defitrack.evm.contract.multicall.MultiCallElement
import org.web3j.abi.TypeReference
import org.web3j.abi.datatypes.Address
import org.web3j.abi.datatypes.Function
import org.web3j.abi.datatypes.generated.Uint256
import java.math.BigInteger

class MasterChefBasedContract(
    private val rewardTokenName: String,
    private val perSecondName: String,
    private val pendingName: String,
    blockchainGateway: BlockchainGateway,
    address: String
) : EvmContract(
    blockchainGateway, "", address
) {

    suspend fun poolLength(): Int {
        return (readWithoutAbi(
            "poolLength",
            outputs = listOf(TypeReference.create(Uint256::class.java))
        )[0].value as BigInteger).toInt()
    }

    suspend fun poolInfos(): List<MasterChefPoolInfo> {
        val multicalls = (0 until poolLength()).map { poolIndex ->
            MultiCallElement(
                createFunction(
                    "poolInfo",
                    inputs = listOf(poolIndex.toBigInteger().toUint256()),
                    outputs = listOf(
                        address(),
                        uint256(),
                        uint256(),
                        uint256()
                    )
                ),
                this.address
            )
        }

        val results = this.blockchainGateway.readMultiCall(
            multicalls
        )
        return results.map { retVal ->
            MasterChefPoolInfo(
                retVal[0].value as String,
                retVal[1].value as BigInteger,
                retVal[2].value as BigInteger,
                retVal[3].value as BigInteger,
            )
        }
    }

    suspend fun getLpTokenForPoolId(poolIndex: Int): MasterChefPoolInfo = poolInfos()[poolIndex]

    suspend fun rewardToken(): String {
        return readWithoutAbi(
            rewardTokenName,
            outputs = listOf(TypeReference.create(Address::class.java))
        )[0].value as String
    }


    suspend fun totalAllocPoint(): BigInteger {
        return readWithoutAbi(
            "totalAllocPoint",
            outputs = listOf(TypeReference.create(Uint256::class.java))
        )[0].value as BigInteger
    }

    suspend fun pending(poolId: Int, user: String): BigInteger {
        return readWithoutAbi(
            method = pendingName,
            inputs = listOf(
                poolId.toBigInteger().toUint256(),
                user.toAddress()
            ),
            outputs = listOf(TypeReference.create(Uint256::class.java))
        )[0].value as BigInteger
    }

    suspend fun perSecond(): BigInteger {
        return readWithoutAbi(
            perSecondName,
            outputs = listOf(TypeReference.create(Uint256::class.java))
        )[0].value as BigInteger
    }

    fun userInfoFunction(poolId: Int, user: String): Function {
        return createFunction(
            "userInfo",
            listOf(
                poolId.toBigInteger().toUint256(),
                user.toAddress()
            ),
            listOf(
                TypeReference.create(Uint256::class.java),
                TypeReference.create(Uint256::class.java)
            )
        )
    }
}