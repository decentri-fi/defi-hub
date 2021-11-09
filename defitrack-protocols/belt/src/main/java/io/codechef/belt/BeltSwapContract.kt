package io.defitrack.belt

import io.defitrack.ethereumbased.contract.SolidityBasedContractAccessor
import io.defitrack.ethereumbased.contract.SolidityBasedContractAccessor.Companion.toAddress
import io.defitrack.ethereumbased.contract.SolidityBasedContractAccessor.Companion.toInt128
import io.defitrack.ethereumbased.contract.SolidityBasedContractAccessor.Companion.toUint256
import io.defitrack.ethereumbased.contract.SolidityBasedContractAccessor.Companion.toUint8
import io.defitrack.ethereumbased.contract.SolidityContract
import org.web3j.abi.TypeReference
import org.web3j.abi.datatypes.Address
import org.web3j.abi.datatypes.generated.Uint256
import org.web3j.abi.datatypes.generated.Uint8
import java.math.BigInteger

class BeltSwapContract(
    solidityBasedContractAccessor: SolidityBasedContractAccessor,
    abi: String, address: String
) : SolidityContract(solidityBasedContractAccessor, abi, address) {


    fun calculateSwap(from: BigInteger, to: BigInteger, amount: BigInteger): BigInteger {
        return read(
            "get_dy_underlying",
            listOf(
                from.toInt128(),
                to.toInt128(),
                amount.toUint256()
            ),
            listOf(
                TypeReference.create(Uint256::class.java)
            )
        )[0].value as BigInteger
    }

    fun getTokenIndex(tokenAddress: String): BigInteger {
        return read(
            "getTokenIndex",
            listOf(
                tokenAddress.toAddress()
            ),
            listOf(
                TypeReference.create(Uint8::class.java)
            )
        )[0].value as BigInteger
    }

    fun getToken(index: BigInteger): String {
        return read(
            "getToken",
            listOf(
                index.toUint8()
            ),
            listOf(
                TypeReference.create(Address::class.java)
            )
        )[0].value as String
    }
}