package io.defitrack.erc20.application.repository

import io.defitrack.common.network.Network

data class TokenListResponse(
    var tokens: List<TokenListEntry>
)

data class TokenListEntry(
    val chainId: Int,
    val name: String,
    val logoURI: String?,
    val address: String,
    val extensions: Extensions?
) {
    fun getAddressesToNetworks(): Map<Network, String> {
        val extensionAddresses = getExtensionAddresses()

        val tokenEntry = Network.fromChainId(chainId)?.let { network ->
            listOf(network to address)
        } ?: emptyList()

        return extensionAddresses.toMap()+ tokenEntry.toMap()
    }

    private fun getExtensionAddresses() = extensions?.bridgeInfo?.entries?.mapNotNull {
        Network.fromChainId(it.key.toInt())?.let { network ->
            network to it.value.tokenAddress
        }
    } ?: emptyList()
}

data class Extensions(
    val bridgeInfo: Map<String, BridgeInfo>?
)

data class BridgeInfo(
    val tokenAddress: String
)