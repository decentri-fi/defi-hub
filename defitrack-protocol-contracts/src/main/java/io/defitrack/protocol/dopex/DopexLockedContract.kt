package io.defitrack.protocol.dopex

import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.ERC20Contract

context (BlockchainGateway)
class DopexLockedContract(address: String) : ERC20Contract(address) {

    //todo: implement
    //0x80789d252a288e93b01d82373d767d71a75d9f16 @ arbitrum

    //locked__end: (address) -> uint256 (time of unlock)

    //balanceOf

    //underlying: token: () -> address

    //totalsupply

    //name: veDPS
}