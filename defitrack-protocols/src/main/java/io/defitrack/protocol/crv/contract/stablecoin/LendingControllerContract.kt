package io.defitrack.protocol.crv.contract.stablecoin

import arrow.core.nel
import io.defitrack.abi.TypeUtils.Companion.address
import io.defitrack.abi.TypeUtils.Companion.dynamicArray
import io.defitrack.abi.TypeUtils.Companion.toAddress
import io.defitrack.abi.TypeUtils.Companion.uint256
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.ContractCall
import io.defitrack.evm.contract.EvmContract
import org.web3j.abi.TypeReference
import org.web3j.abi.datatypes.DynamicArray
import org.web3j.abi.datatypes.Function
import org.web3j.abi.datatypes.generated.StaticArray4
import org.web3j.abi.datatypes.generated.Uint256

class LendingControllerContract(
    blockchainGateway: BlockchainGateway, address: String
) : EvmContract(
    blockchainGateway, address
) {

    val collateral = constant<String>("collateral_token", address())

    fun debtFn(user: String): ContractCall {
        return createFunction(
            "debt",
            user.toAddress().nel(),
            uint256().nel()
        )
    }

    fun userState(user: String): ContractCall {
        return createFunction(
            "user_state",
            user.toAddress().nel(),
            listOf(
                object : TypeReference<StaticArray4<Uint256>>(false) {}
            )
        )
    }
}