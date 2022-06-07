package io.defitrack.protocol.ribbon.contract

import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.ERC20Contract

class RibbonVaultContract(
    blockchainGateway: BlockchainGateway,
    abi: String,
    address: String
) : ERC20Contract(blockchainGateway, abi, address)