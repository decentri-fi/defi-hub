package io.defitrack.abi

import org.web3j.abi.TypeReference
import org.web3j.abi.datatypes.*
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

        fun uint256(indexed: Boolean = false): TypeReference<Uint256> {
            return TypeReference.create(Uint256::class.java, indexed)
        }

        fun uint88(indexed: Boolean = false): TypeReference<Uint88> {
            return TypeReference.create(Uint88::class.java, indexed)
        }

        fun int24(indexed: Boolean = false): TypeReference<Int24> {
            return TypeReference.create(Int24::class.java, indexed)
        }

        fun address(indexed: Boolean = false): TypeReference<Address> {
            return TypeReference.create(Address::class.java, indexed)
        }

        fun string(indexed: Boolean = false): TypeReference<Utf8String> {
            return TypeReference.create(Utf8String::class.java, indexed)
        }

        fun bytes32(indexed: Boolean = false): TypeReference<Bytes32> {
            return TypeReference.create(Bytes32::class.java, indexed)
        }

        fun uint40(): TypeReference<Uint40> {
            return TypeReference.create(Uint40::class.java)
        }

        fun uint24(indexed: Boolean): TypeReference<Uint24> {
            return TypeReference.create(Uint24::class.java, indexed)
        }

        fun uint64(): TypeReference<Uint64> {
            return TypeReference.create(Uint64::class.java)
        }

        fun uint128(): TypeReference<Uint128> {
            return TypeReference.create(Uint128::class.java)
        }

        fun uint32(): TypeReference<Uint32> {
            return TypeReference.create(Uint32::class.java)
        }

        fun uint16(): TypeReference<Uint32> {
            return TypeReference.create(Uint32::class.java)
        }

        fun bool(): TypeReference<Bool> {
            return TypeReference.create(Bool::class.java)
        }

        fun String.toAddress(): Address {
            return Address(this)
        }

        fun String.toUtf8String(): Utf8String {
            return Utf8String(this)
        }

        fun Boolean.toBool(): Bool {
            return Bool(this)
        }
    }

}