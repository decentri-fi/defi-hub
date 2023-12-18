package io.defitrack.protocol.crv.contract.stablecoin

import arrow.core.nel
import io.defitrack.abi.TypeUtils.Companion.address
import io.defitrack.abi.TypeUtils.Companion.toAddress
import io.defitrack.abi.TypeUtils.Companion.uint256
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.ContractCall
import io.defitrack.evm.contract.EvmContract
import io.defitrack.evm.position.Position
import org.web3j.abi.TypeReference
import org.web3j.abi.datatypes.Type
import org.web3j.abi.datatypes.generated.StaticArray4
import org.web3j.abi.datatypes.generated.Uint256
import java.math.BigInteger

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

    fun positionFromUserState(retVal: List<Type<*>>): Position {
        val bal = (retVal.first().value as List<Uint256>).first().value as BigInteger
        return if (bal > BigInteger.ZERO) {
            Position(
                bal,
                bal
            )
        } else {
            Position.ZERO
        }
    }
}