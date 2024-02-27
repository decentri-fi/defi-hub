package io.defitrack.protocol.etherfi

import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.ERC20Contract

context(BlockchainGateway)
class EEthContract( address: String
) : ERC20Contract(address)