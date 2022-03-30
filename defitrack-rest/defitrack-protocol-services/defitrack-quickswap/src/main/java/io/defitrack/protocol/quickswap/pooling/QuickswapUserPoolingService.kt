package io.defitrack.protocol.quickswap.pooling

import io.defitrack.abi.ABIResource
import io.defitrack.common.network.Network
import io.defitrack.common.utils.BigDecimalExtensions.dividePrecisely
import io.defitrack.evm.contract.ContractAccessorGateway
import io.defitrack.evm.contract.ERC20Contract
import io.defitrack.evm.contract.multicall.MultiCallElement
import io.defitrack.polygon.config.PolygonContractAccessorConfig
import io.defitrack.pool.UserPoolingService
import io.defitrack.pool.domain.PoolingElement
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.quickswap.QuickswapService
import io.defitrack.token.TokenType
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.math.BigInteger

@Service
class QuickswapUserPoolingService(
    private val quickswapService: QuickswapService,
    private val contractAccessorGateway: ContractAccessorGateway,
    private val abiservice: ABIResource
) : UserPoolingService() {

    val erc20ABI by lazy {
        abiservice.getABI("general/ERC20.json")
    }

    override suspend fun fetchUserPoolings(address: String): List<PoolingElement> {

        val gateway = contractAccessorGateway.getGateway(getNetwork())

        val tokens = quickswapService.getPairs()

        return gateway.readMultiCall(
            tokens.map { token ->
                MultiCallElement(
                    ERC20Contract(
                        gateway,
                        erc20ABI,
                        token.id
                    ).balanceOfMethod(address),
                    token.id
                )
            }
        ).mapIndexed { index, item ->


            val want = tokens[index]
            val balance = item[0].value as BigInteger

            if (balance > BigInteger.ZERO) {
                val token1 = want.token0
                val token2 = want.token1
                val amount = balance.toBigDecimal().dividePrecisely(BigDecimal.TEN.pow(18))
                PoolingElement(
                    lpAddress = want.id,
                    amount = amount,
                    name = "${token1.symbol}/${token2.symbol} LP",
                    network = getNetwork(),
                    protocol = Protocol.QUICKSWAP,
                    symbol = "${token1.symbol}/${token2.symbol}",
                    tokenType = TokenType.UNISWAP,
                    id = "quickswap-polygon-${want.id}",
                )
            } else {
                null
            }
        }.filterNotNull()
    }


    override fun getProtocol(): Protocol {
        return Protocol.QUICKSWAP
    }

    override fun getNetwork(): Network {
        return Network.POLYGON
    }
}