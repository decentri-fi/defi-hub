package io.defitrack.protocol.aave.v3.contract

import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.abi.TypeUtils.Companion.toAddress
import io.defitrack.abi.TypeUtils.Companion.uint256
import io.defitrack.abi.TypeUtils.Companion.uint40
import io.defitrack.evm.contract.DeprecatedEvmContract
import io.defitrack.protocol.aave.v3.domain.ReserveData
import io.defitrack.protocol.aave.v3.domain.ReserveTokenAddresses
import java.math.BigInteger
import io.defitrack.abi.TypeUtils.Companion.address

class PoolDataProvider(
    blockchainGateway: BlockchainGateway,
    address: String
) : DeprecatedEvmContract(blockchainGateway, address) {

    suspend fun getReserveTokensAddresses(asset: String): ReserveTokenAddresses {
        val retVal = read(
            "getReserveTokensAddresses",
            listOf(asset.toAddress()),
            listOf(address(), address(), address())
        )

        return ReserveTokenAddresses(
            retVal[0].value as String,
            retVal[1].value as String,
            retVal[2].value as String
        )
    }

    suspend fun getATokenTotalSupply(asset: String): BigInteger {
        return read(
            "getATokenTotalSupply",
            listOf(asset.toAddress()),
            listOf(uint256())
        )[0].value as BigInteger
    }

    suspend fun getReserveData(asset: String): ReserveData {
        val result = read(
            "getReserveData",
            listOf(
                asset.toAddress()
            ),
            listOf(
                uint256(),
                uint256(),
                uint256(),
                uint256(),
                uint256(),
                uint256(),
                uint256(),
                uint256(),
                uint256(),
                uint256(),
                uint256(),
                uint40(),
            )
        )
        return ReserveData(
            result[0].value as BigInteger,
            result[1].value as BigInteger,
            result[2].value as BigInteger,
            result[3].value as BigInteger,
            result[4].value as BigInteger,
            result[5].value as BigInteger,
            result[6].value as BigInteger,
            result[7].value as BigInteger,
            result[8].value as BigInteger,
            result[9].value as BigInteger,
            result[10].value as BigInteger,
            result[11].value as BigInteger,
        )
    }
}