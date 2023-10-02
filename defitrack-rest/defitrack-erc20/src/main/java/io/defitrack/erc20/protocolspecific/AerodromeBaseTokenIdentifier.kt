package io.defitrack.erc20.protocolspecific

import io.defitrack.common.network.Network
import io.defitrack.common.utils.AsyncUtils.lazyAsync
import io.defitrack.erc20.ERC20
import io.defitrack.erc20.LpContractReader
import io.defitrack.evm.contract.BlockchainGatewayProvider
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.contract.PoolFactoryContract
import io.defitrack.token.TokenType
import org.springframework.stereotype.Component

@Component
class AerodromeBaseTokenIdentifier(
    private val blockchainGatewayProvider: BlockchainGatewayProvider,
    lpContractReader: LpContractReader
) : DefaultLpIdentifier(
    Protocol.AERODROME, TokenType.AERODROME, lpContractReader
) {

    private val poolFactoryAddress: String = "0x420DD381b31aEf6683db6B902084cB0FFECe40Da"

    private val poolFactoryContract = lazyAsync {
        PoolFactoryContract(
            blockchainGateway = blockchainGatewayProvider.getGateway(Network.BASE),
            contractAddress = poolFactoryAddress
        ).allPools().map(String::lowercase)
    }

    override suspend fun isProtocolToken(token: ERC20): Boolean {
        return token.network == Network.BASE && poolFactoryContract.await().contains(token.address.lowercase())
    }

}