package io.defitrack.evm.web3j

import org.web3j.protocol.Web3j

interface EvmGateway {
    fun web3j(): Web3j
}