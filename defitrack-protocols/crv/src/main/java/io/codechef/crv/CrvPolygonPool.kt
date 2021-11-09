package io.defitrack.crv

import io.defitrack.ethereumbased.contract.SolidityBasedContractAccessor
import io.defitrack.ethereumbased.contract.SolidityContract

class CrvPolygonPool(
    ethereumContractAccessor: SolidityBasedContractAccessor,
    abi: String,
    address: String
) : SolidityContract(ethereumContractAccessor, abi, address) {


}