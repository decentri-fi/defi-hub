package io.defitrack.protocol.sushiswap.xsushi

import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.ERC20Contract

context(BlockchainGateway)
class XSushiContract(address: String) :
    ERC20Contract(address)