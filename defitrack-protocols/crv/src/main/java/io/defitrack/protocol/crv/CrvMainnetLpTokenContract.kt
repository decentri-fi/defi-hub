package io.defitrack.protocol.crv

import io.defitrack.evm.contract.EvmContract
import io.defitrack.evm.contract.BlockchainGateway
import java.math.BigInteger

class CrvMainnetLpTokenContract(
    ethereumContractAccessor: BlockchainGateway,
    abi: String,
    address: String
) : EvmContract(ethereumContractAccessor, abi, address) {

    val symbol: String by lazy {
        read(
            "symbol"
        )[0].value as String
    }

    val name: String by lazy {
        read(
            "name"
        )[0].value as String
    }

    val decimals: BigInteger by lazy {
        read(
            "decimals"
        )[0].value as BigInteger
    }
}