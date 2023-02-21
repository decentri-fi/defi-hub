package io.defitrack.protocol

import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.ERC20Contract

class CowswapVirtualToken(
    blockchainGateway: BlockchainGateway, address: String
) : ERC20Contract(
    blockchainGateway, "", address
) {
}