package io.defitrack.erc20

import io.defitrack.common.utils.Refreshable.Companion.refreshable
import io.defitrack.protocol.Protocol
import io.defitrack.token.TokenInformation
import io.defitrack.token.TokenType
import org.springframework.stereotype.Component

@Component
class ERC20ToTokenInformationMapper(
    private val erC20ContractReader: ERC20ContractReader
) {
    fun map(erC20: ERC20, type: TokenType, protocol: Protocol): TokenInformation {
        return TokenInformation(
            name = erC20.name,
            address = erC20.address,
            symbol = erC20.symbol,
            decimals = erC20.decimals,
            type = type,
            protocol = protocol,
            network = erC20.network,
            totalSupply = refreshable(erC20.totalSupply) {
                erC20ContractReader.getERC20(erC20.network, erC20.address).totalSupply
            }
        )
    }
}