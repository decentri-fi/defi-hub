package io.defitrack.protocol.swell

import io.defitrack.abi.TypeUtils.Companion.uint256
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.ERC20Contract
import java.math.BigInteger

class SwethContract(
    blockchainGateway: BlockchainGateway, address: String
): ERC20Contract(
    blockchainGateway, address
)  {

    val rate = constant<BigInteger>("getRate", uint256())
    val totalEthDeposited = constant<BigInteger>("totalETHDeposited", uint256())

}