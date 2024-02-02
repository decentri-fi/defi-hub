package io.defitrack.protocol.sushiswap.xsushi

import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.ERC20Contract

class XSushiContract(blockchainGateway: BlockchainGateway, address: String) :
    ERC20Contract(blockchainGateway, address)