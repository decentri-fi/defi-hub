package io.defitrack.claimable.domain

import io.defitrack.evm.contract.ContractCall
import io.defitrack.token.FungibleToken
import org.web3j.abi.datatypes.Type
import java.math.BigInteger

data class Reward(
    val token: FungibleToken,
    val getRewardFunction: suspend (String) -> ContractCall,
    val extractAmountFromRewardFunction: suspend (List<Type<*>>, String) -> BigInteger = { result, _ ->
        result[0].value as BigInteger
    },
)