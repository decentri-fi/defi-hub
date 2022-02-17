package io.defitrack.protocol.uniswap.pooling

import io.defitrack.common.network.Network
import io.defitrack.common.utils.BigDecimalExtensions.dividePrecisely
import io.defitrack.ethereum.config.EthereumContractAccessor
import io.defitrack.pool.UserPoolingService
import io.defitrack.pool.domain.PoolingElement
import io.defitrack.protocol.Protocol
import io.defitrack.token.ERC20Resource
import io.defitrack.token.TokenType
import io.defitrack.uniswap.EthereumUniswapV2Service
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.math.BigInteger

@Service
class UniswapUserPoolingService(
    private val etrhereumUniswapV2Service: EthereumUniswapV2Service,
    private val erC20Resource: ERC20Resource,
    private val ethereumContractAccessor: EthereumContractAccessor,
) : UserPoolingService() {

    override suspend fun fetchUserPoolings(address: String): List<PoolingElement> {
        val allPairs = etrhereumUniswapV2Service.getPairs()

        return erC20Resource.getBalancesFor(address, allPairs.map { it.id }, ethereumContractAccessor)
            .mapIndexed { index, balance ->
                if (balance > BigInteger.ZERO) {
                    val want = allPairs[index]

                    val token = erC20Resource.getTokenInformation(getNetwork(), want.id)

                    val token1 = want.token0
                    val token2 = want.token1
                    val amount = balance.toBigDecimal().dividePrecisely(BigDecimal.TEN.pow(token.decimals))

                    PoolingElement(
                        id = "uniswap-ethereum-${want.id}",
                        lpAddress = token.address,
                        amount = amount,
                        name = "${token1.name}/${token2.name} LP",
                        network = getNetwork(),
                        protocol = getProtocol(),
                        symbol = "${token1.symbol}/${token2.symbol}",
                        tokenType = TokenType.UNISWAP
                    )
                } else {
                    null
                }
            }.filterNotNull()
    }

    override fun getProtocol(): Protocol {
        return Protocol.UNISWAP
    }

    override fun getNetwork(): Network {
        return Network.ETHEREUM
    }
}