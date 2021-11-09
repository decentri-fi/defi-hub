package io.defitrack.protocol.beefy.contract

import io.defitrack.ethereumbased.contract.SolidityBasedContractAccessor
import io.defitrack.ethereumbased.contract.SolidityContract
import org.web3j.abi.TypeReference
import org.web3j.abi.datatypes.Address
import org.web3j.abi.datatypes.generated.Uint256
import java.math.BigInteger

class StrategyPolygonQuickLPContract(
    solidityBasedContractAccessor: SolidityBasedContractAccessor,
    abi: String,
    address: String
) :
    SolidityContract(solidityBasedContractAccessor, abi, address) {

    val rewardPool by lazy {
        read(
            "rewardPool",
            outputs = listOf(TypeReference.create(Address::class.java))
        )[0].value as String
    }
}