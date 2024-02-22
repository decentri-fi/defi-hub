package io.defitrack.protocol.lido

import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.ERC20Contract

class StethContract(
    blockchainGateway: BlockchainGateway, address: String
) : ERC20Contract(
    blockchainGateway, address
)