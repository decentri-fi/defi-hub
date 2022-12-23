package io.defitrack.protocol.set

import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.ERC20Contract
import org.web3j.abi.TypeReference
import org.web3j.abi.datatypes.Address
import org.web3j.abi.datatypes.DynamicArray
import org.web3j.abi.datatypes.DynamicBytes
import org.web3j.abi.datatypes.DynamicStruct
import org.web3j.abi.datatypes.generated.Int256
import org.web3j.abi.datatypes.generated.Int8
import java.math.BigInteger

class SetTokenContract(
    blockchainGateway: BlockchainGateway,
    address: String
) : ERC20Contract(
    blockchainGateway, "", address
) {

    suspend fun getPositions(): List<Position> {
        return readWithoutAbi(
            "getPositions",
            outputs = listOf(
                object : TypeReference<DynamicArray<Position>>() {}
            )
        )[0].value as List<Position>
    }

    class Position : DynamicStruct {

        val token: String
        val component: String
        val amount: BigInteger
        val positionState: BigInteger
        val bytes: ByteArray

        constructor(
            _token: String,
            _component: String,
            _amount: BigInteger,
            _positionState: BigInteger,
            _bytes: ByteArray
        ) : super(Address(_token), Address(_component), Int256(_amount), Int8(_positionState), DynamicBytes(_bytes)) {
            token = _token
            component = _component
            amount = _amount
            positionState = _positionState
            bytes = _bytes
        }

        constructor(
            _token: Address,
            _component: Address,
            _amount: Int256,
            _positionState: Int8,
            _bytes: DynamicBytes
        ) : super(_token, _component, _amount, _positionState, _bytes) {
            token = _token.value
            component = _component.value
            amount = _amount.value
            positionState = _positionState.value
            bytes = _bytes.value
        }
    }
}