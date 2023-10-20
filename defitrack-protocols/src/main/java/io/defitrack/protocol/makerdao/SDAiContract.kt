package io.defitrack.protocol.makerdao

import arrow.core.nonEmptyListOf
import io.defitrack.abi.TypeUtils.Companion.toUint256
import io.defitrack.abi.TypeUtils.Companion.uint256
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.ERC20Contract
import org.web3j.abi.datatypes.Address
import java.math.BigInteger

class SDAiContract(blockchainGateway: BlockchainGateway, address: String) : ERC20Contract(
    blockchainGateway, address
) {

    suspend fun convertToAssets(shares: BigInteger): BigInteger {
        return readSingle<BigInteger>("convertToAssets", inputs = nonEmptyListOf(shares.toUint256()), uint256())
    }
}