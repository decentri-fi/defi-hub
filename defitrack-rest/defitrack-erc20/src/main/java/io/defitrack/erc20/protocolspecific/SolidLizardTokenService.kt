package io.defitrack.erc20.protocolspecific

import io.defitrack.common.network.Network
import io.defitrack.erc20.ERC20
import io.defitrack.erc20.LpContractReader
import io.defitrack.evm.contract.BlockchainGatewayProvider
import io.defitrack.protocol.Protocol
import io.defitrack.token.TokenType
import io.defitrack.uniswap.v2.PairFactoryContract
import kotlinx.coroutines.runBlocking
import org.springframework.stereotype.Service

@Service
class SolidLizardTokenService(
    private val blockchainGatewayProvider: BlockchainGatewayProvider,
    lpContractReader: LpContractReader
) : DefaultLpIdentifier(
    Protocol.SOLIDLIZARD, TokenType.SOLIDLIZARD, lpContractReader
) {

    val arbitrumPools by lazy {
        runBlocking {
            val pairFactoryContract = PairFactoryContract(
                blockchainGateway = blockchainGatewayProvider.getGateway(Network.ARBITRUM),
                contractAddress = "0x734d84631f00dc0d3fcd18b04b6cf42bfd407074"
            )
            pairFactoryContract.allPairs()
        }
    }

    override suspend fun isProtocolToken(token: ERC20): Boolean {
        return when (token.network) {
            Network.ARBITRUM -> arbitrumPools.contains(token.address)
            else -> false
        }
    }
}