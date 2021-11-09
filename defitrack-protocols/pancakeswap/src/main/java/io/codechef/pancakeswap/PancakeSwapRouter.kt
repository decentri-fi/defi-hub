package io.defitrack.pancakeswap

import io.defitrack.bsc.BscContractAccessor
import io.defitrack.ethereumbased.contract.SolidityBasedContractAccessor.Companion.toAddress
import io.defitrack.ethereumbased.contract.SolidityBasedContractAccessor.Companion.toUint256
import io.defitrack.ethereumbased.contract.SolidityContract
import io.defitrack.protocol.Swapper
import org.web3j.abi.TypeReference
import org.web3j.abi.datatypes.DynamicArray
import org.web3j.abi.datatypes.Uint
import java.math.BigInteger

class PancakeSwapRouter(
    routerAddress: String,
    abi: String,
    bscContractAccessor: BscContractAccessor
) : SolidityContract(bscContractAccessor, abi, routerAddress), Swapper {

    override fun getExpectedTokens(from: String, to: String, amountIn: BigInteger): BigInteger {
        return (read(
            method = "getAmountsOut",
            inputs = listOf(
                amountIn.toUint256(),
                DynamicArray(
                    from.toAddress(),
                    to.toAddress()
                )
            ),
            outputs = listOf(
                object : TypeReference<DynamicArray<Uint>>() {}
            )
        )[0].value as ArrayList<Uint>)[1].value as BigInteger
    }
}