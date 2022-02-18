package io.defitrack.erc20

import io.defitrack.abi.ABIResource
import io.defitrack.common.network.Network
import io.defitrack.evm.contract.EvmContractAccessor
import io.defitrack.protocol.HopService
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.contract.HopLpTokenContract
import io.defitrack.token.TokenInformation
import io.defitrack.token.TokenType
import org.springframework.stereotype.Component

@Component
class HopTokenService(
    private val abiResource: ABIResource,
    private val contractAccessors: List<EvmContractAccessor>,
    private val hopService: HopService,
    private val erC20Service: ERC20Service
) {

    fun getTokenInformation(
        address: String,
        network: Network
    ): TokenInformation {
        return hopService.getLps(network).find {
            it.lpToken.lowercase() == address.lowercase()
        }!!.let { hopLpToken ->

            val saddleToken = HopLpTokenContract(
                evmContractAccessor = getContractAccessor(network),
                abiResource.getABI("hop/SaddleToken.json"),
                address
            )

            val token0 = erC20Service.getERC20(network, hopLpToken.canonicalToken)
            val token1 = erC20Service.getERC20(network, hopLpToken.hToken)

            TokenInformation(
                name = saddleToken.name,
                symbol = saddleToken.symbol,
                tokenInformation0 = token0?.toToken(),
                tokenInformation1 = token1?.toToken(),
                address = address,
                decimals = saddleToken.decimals,
                totalSupply = saddleToken.totalSupply,
                type = TokenType.HOP,
                protocol = Protocol.HOP
            )
        }
    }

    fun getContractAccessor(network: Network): EvmContractAccessor {
        return contractAccessors.find {
            it.getNetwork() == network
        } ?: throw IllegalArgumentException("$network not supported")
    }
}