package io.defitrack.belt

import io.defitrack.ethereumbased.contract.ReadRequest
import io.defitrack.ethereumbased.contract.SolidityBasedContractAccessor
import io.defitrack.ethereumbased.contract.SolidityBasedContractAccessor.Companion.toAddress
import io.defitrack.ethereumbased.contract.SolidityBasedContractAccessor.Companion.toUint256
import io.defitrack.ethereumbased.contract.SolidityContract
import io.defitrack.protocol.staking.Token
import org.web3j.abi.TypeReference
import org.web3j.abi.datatypes.Address
import org.web3j.abi.datatypes.generated.Uint256
import java.math.BigInteger

class MasterBeltBscContract(
    bscContractAccessor: SolidityBasedContractAccessor,
    abi: String,
    address: String,
) : SolidityContract(bscContractAccessor, abi, address) {

    val poolLength by lazy {
        (read(
            "poolLength",
            outputs = listOf(TypeReference.create(Uint256::class.java))
        )[0].value as BigInteger).toInt()
    }

    val BELT by lazy {
        read(
            "BELT",
            outputs = listOf(TypeReference.create(Address::class.java))
        )[0].value as String
    }


    fun getLpTokenForPoolId(poolIndex: Int): String {
        return read(
            "poolInfo",
            inputs = listOf(poolIndex.toBigInteger().toUint256()),
            outputs = listOf(
                TypeReference.create(Address::class.java),
                TypeReference.create(Uint256::class.java),
                TypeReference.create(Uint256::class.java),
                TypeReference.create(Uint256::class.java),
                TypeReference.create(Address::class.java),
            )
        )[0].value as String
    }

    fun userInfo(poolIds: List<BigInteger>, address: String): List<BigInteger> {
        return readMultiple(poolIds.map { poolId ->
            ReadRequest(
                "stakedWantTokens",
                inputs = listOf(
                    poolId.toUint256(),
                    address.toAddress()
                ),
                outputs = listOf(
                    TypeReference.create(Uint256::class.java),
                )
            )
        }).map { result ->
            result[0].value as BigInteger
        }
    }
}

