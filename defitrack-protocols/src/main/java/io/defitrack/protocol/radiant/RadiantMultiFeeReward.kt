package io.defitrack.protocol.radiant

import org.web3j.abi.datatypes.Address
import org.web3j.abi.datatypes.DynamicStruct
import org.web3j.abi.datatypes.StaticStruct
import org.web3j.abi.datatypes.Type
import org.web3j.abi.datatypes.generated.Uint256

class RadiantMultiFeeReward : StaticStruct {

      @JvmField
      val rewardAddress: Address
      @JvmField
      val amount: Uint256

      constructor(rewardAddress: Address, amount: Uint256) : super(rewardAddress, amount) {
            this.rewardAddress = rewardAddress
            this.amount = amount
      }
}