package io.defitrack.erc20

import io.defitrack.common.utils.FormatUtilsExtensions.asEth
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.ERC20Contract
import io.defitrack.network.NetworkVO
import io.defitrack.protocol.ProtocolVO
import io.defitrack.token.TokenType
import java.math.BigDecimal
import java.math.BigInteger

class FungibleToken(
    val network: NetworkVO,
    val logo: String? = null,
    val name: String,
    val symbol: String,
    val address: String,
    val decimals: Int,
    val type: TokenType,
    val totalSupply: BigInteger = BigInteger.ZERO,
    val underlyingTokens: List<FungibleToken> = emptyList(),
    val protocol: ProtocolVO? = null
) {

    fun totalDecimalSupply(): BigDecimal {
        return totalSupply.asEth(decimals)
    }

    fun asERC20Contract(blockchainGateway: BlockchainGateway): ERC20Contract {
        return ERC20Contract(blockchainGateway, address)
    }
}