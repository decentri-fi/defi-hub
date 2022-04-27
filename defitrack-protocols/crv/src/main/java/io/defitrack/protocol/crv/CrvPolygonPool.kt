package io.defitrack.protocol.crv

import io.defitrack.evm.contract.EvmContract
import io.defitrack.evm.contract.BlockchainGateway

class CrvPolygonPool(
    ethereumContractAccessor: BlockchainGateway,
    abi: String,
    address: String
) : EvmContract(ethereumContractAccessor, abi, address)