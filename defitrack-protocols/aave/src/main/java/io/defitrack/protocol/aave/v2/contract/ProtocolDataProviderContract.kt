package io.defitrack.protocol.aave.v2.contract

import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.abi.TypeUtils.Companion.toAddress
import io.defitrack.evm.contract.EvmContract
import io.defitrack.evm.contract.multicall.MultiCallElement
import org.web3j.abi.TypeReference
import org.web3j.abi.datatypes.*
import org.web3j.abi.datatypes.generated.Uint256
import java.math.BigInteger

class ReserveTokenData(
    symbol: String, address: String
) : DynamicStruct(
    Utf8String(symbol), Address(address)
) {
    constructor(symbol: Utf8String, address: Address) : this(symbol.value, address.value)
}


class ProtocolDataProviderContract(blockchainGateway: BlockchainGateway, abi: String, address: String) :
    EvmContract(
        blockchainGateway, abi, address
    ) {

    fun getReserveConfigurationData(address: String): List<Type<*>> {
        return readWithAbi(
            "getReserveConfigurationData",
            inputs = listOf(address.toAddress()),
            outputs = listOf(
                TypeReference.create(Uint256::class.java),
                TypeReference.create(Uint256::class.java),
                TypeReference.create(Uint256::class.java),
                TypeReference.create(Uint256::class.java),
                TypeReference.create(Uint256::class.java),
                TypeReference.create(Bool::class.java),
                TypeReference.create(Bool::class.java),
                TypeReference.create(Bool::class.java),
                TypeReference.create(Bool::class.java),
                TypeReference.create(Bool::class.java),
            )
        )
    }

    val allReservesTokens: List<ReserveToken> by lazy {
        val retValk = readWithAbi(
            "getAllReservesTokens",
            inputs = emptyList(),
            outputs = listOf(
                object : TypeReference<DynamicArray<ReserveTokenData>>() {})
        )

        val theList = (retValk[0].value as List<ReserveTokenData>)
        theList.map {
            ReserveToken(
                it.value[0].value as String,
                it.value[1].value as String,
                18,
                liquidationBonus = getReserveConfigurationData(
                    it.value[1].value as String,
                )[3].value as BigInteger
            )
        }
    }

    fun getReserveDataFunction(
        user: String,
        asset: String
    ): MultiCallElement {
        return MultiCallElement(
            createFunctionWithAbi(
                "getUserReserveData",
                inputs = listOf(
                    asset.toAddress(),
                    user.toAddress(),
                ),
                outputs = listOf(
                    TypeReference.create(Uint256::class.java),
                    TypeReference.create(Uint256::class.java),
                    TypeReference.create(Uint256::class.java),
                    TypeReference.create(Uint256::class.java),
                    TypeReference.create(Uint256::class.java),
                    TypeReference.create(Uint256::class.java),
                    TypeReference.create(Uint256::class.java),
                    TypeReference.create(Uint256::class.java),
                    TypeReference.create(Bool::class.java),
                )
            ), this.address
        )
    }

    fun getReserveData(user: String, asset: ReserveToken): UserReserveData {
        val retVal = readWithAbi(
            "getUserReserveData",
            inputs = listOf(
                asset.address.toAddress(),
                user.toAddress(),
            ),
            outputs = listOf(
                TypeReference.create(Uint256::class.java),
                TypeReference.create(Uint256::class.java),
                TypeReference.create(Uint256::class.java),
                TypeReference.create(Uint256::class.java),
                TypeReference.create(Uint256::class.java),
                TypeReference.create(Uint256::class.java),
                TypeReference.create(Uint256::class.java),
                TypeReference.create(Uint256::class.java),
                TypeReference.create(Bool::class.java),
            )
        )
        return with(retVal) {
            UserReserveData(
                this[0].value as BigInteger,
                this[1].value as BigInteger,
                this[2].value as BigInteger,
                this[3].value as BigInteger,
                this[4].value as BigInteger,
                this[5].value as BigInteger,
                this[6].value as BigInteger,
                this[7].value as BigInteger,
                this[8].value as Boolean,
                asset
            )
        }
    }
}