package io.defitrack.protocol.uncx

import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.EvmContract

context(BlockchainGateway)
class LockerContract(address: String) : EvmContract(address) {

    /*
    addresses:
    0x663a5c229c09b049e36dcc11a9b0d4a8eb9db214

     */

    //getUserNumLockedTokens: (String) -> BigInteger

    //getUserLockedTokenAtIndex: (String, BigInteger) -> String

    //getUserNumLocksForToken: (String: user, String: lp) -> BigInteger

    //getUserLockForTokenAtIndex: (String: user, String: lp, BigInteger: index) -> Lock

    /**
     * Lock
     *
     * uint256,
     * uint256,
     * uint256,
     * uint256,
     * uint256,
     * address
     */


}