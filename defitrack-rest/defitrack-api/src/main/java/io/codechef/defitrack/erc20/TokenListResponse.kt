package io.codechef.defitrack.erc20

class TokenListResponse(
    var tokens: List<TokenListEntry>
)

class TokenListEntry(
    val chainId: Int,
    val name: String,
    val logoURI: String?,
    val address: String,
)