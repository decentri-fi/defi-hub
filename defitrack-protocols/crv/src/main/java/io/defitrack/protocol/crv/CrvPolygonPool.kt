package io.defitrack.protocol.crv

import io.defitrack.evm.contract.EvmContract
import io.defitrack.evm.contract.EvmContractAccessor

class CrvPolygonPool(
    ethereumContractAccessor: EvmContractAccessor,
    abi: String,
    address: String
) : EvmContract(ethereumContractAccessor, abi, address)