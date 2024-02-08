package io.defitrack.erc20.application.repository

import io.defitrack.common.network.Network

interface ERC20TokenListResource {
    fun allTokens(network: Network): List<String>
}