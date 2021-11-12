package io.defitrack.protocol.crv

import io.defitrack.ethereumbased.contract.EvmContract
import io.defitrack.ethereumbased.contract.EvmContractAccessor

class CrvPolygonPool(
    ethereumContractAccessor: EvmContractAccessor,
    abi: String,
    address: String
) : EvmContract(ethereumContractAccessor, abi, address)