package io.defitrack.protocol.shibaswap

import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.DeprecatedEvmContract

context(BlockchainGateway)
class ShibaMasterchefContract(
    address: String
) : DeprecatedEvmContract(this, address) {

    //TODO: implement

    //0x94235659cf8b805b2c658f9ea2d6d6ddbb17c8d7 @ ethereum

    //reward -> bone: () -> address

    //pendingBone: (uint256, address) -> uint256

    //poolInfo: (uint256) -> (address, uint256, uint256 (lastrewardblock), uint256)

    //poolLength: () -> uint256

    //userinfo: (uint256, address) -> (uint256: amount, uint256: rewardDebt)
}