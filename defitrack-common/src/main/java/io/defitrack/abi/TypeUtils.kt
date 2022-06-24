package io.defitrack.abi

import org.web3j.abi.TypeReference
import org.web3j.abi.datatypes.Address
import org.web3j.abi.datatypes.Bool
import org.web3j.abi.datatypes.Type
import org.web3j.abi.datatypes.generated.*
import java.math.BigInteger

class TypeUtils {

    companion object {

        fun BigInteger.toUint256(): Uint256 {
            return Uint256(this)
        }

        fun BigInteger.toUint8(): Uint8 {
            return Uint8(this)
        }

        fun BigInteger.toUint16(): Uint16 {
            return Uint16(this)
        }

        fun BigInteger.Int128(): Int128 {
            return Int128(this)
        }

        fun BigInteger.toInt128(): Int128 {
            return Int128(this)
        }

        fun uint256(): TypeReference<Uint256> {
            return TypeReference.create(Uint256::class.java)
        }

        fun address(): TypeReference<Address> {
            return TypeReference.create(Address::class.java)
        }

        fun uint40(): TypeReference<Uint40> {
            return TypeReference.create(Uint40::class.java)
        }


        fun bool(): TypeReference<Bool> {
            return TypeReference.create(Bool::class.java)
        }

        fun String.toAddress(): Address {
            return Address(this)
        }

        fun Boolean.toBool(): Bool {
            return Bool(this)
        }
    }

}