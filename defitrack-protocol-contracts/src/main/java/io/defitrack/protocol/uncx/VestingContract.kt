package io.defitrack.protocol.uncx

import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.DeprecatedEvmContract

context(BlockchainGateway)
class VestingContract(address: String) : DeprecatedEvmContract(this, address) {

    /*
    addresses:
    0xdba68f07d1b7ca219f78ae8582c213d975c25caf

     */

    //getUserLockedTokensLength: (String) -> BigInteger

    //getUserLockedTokenAtIndex: (String, BigInteger) -> String

    //getUserLocksForTokenLength: (String: user, String: lp) -> BigInteger

    //getUserLockIDForTokenAtIndex: (String: user, String: lp, BigInteger: index) -> Lock

    //getLock: (Uint256: lockID) -> Lock

    /**
     * Lock
     *
     * uint256,
     * address, //token
     * uint256,
     * uint256,
     * uint256,//amount
     * uint256,
     * uint256,
     * uint256, //unlockTime
     * address //user
     * address
     */


    //convertSharesToTokens: (String: token, uint256) -> uint256



}