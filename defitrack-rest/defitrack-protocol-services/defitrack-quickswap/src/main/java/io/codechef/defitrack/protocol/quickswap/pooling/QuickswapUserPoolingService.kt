package io.codechef.defitrack.protocol.quickswap.pooling

import io.codechef.defitrack.pool.UserPoolingService
import io.codechef.defitrack.pool.domain.PoolingElement
import io.defitrack.abi.ABIResource
import io.defitrack.common.network.Network
import io.defitrack.ethereumbased.contract.EvmContractAccessor.Companion.toAddress
import io.defitrack.ethereumbased.contract.multicall.MultiCallElement
import io.defitrack.polygon.config.PolygonContractAccessor
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.staking.TokenType
import io.defitrack.quickswap.QuickswapService
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import org.web3j.abi.TypeReference
import org.web3j.abi.datatypes.generated.Uint256
import java.math.BigDecimal
import java.math.BigInteger
import java.math.RoundingMode

@Service
class QuickswapUserPoolingService(
    private val quickswapService: QuickswapService,
    private val polygonContractAccessor: PolygonContractAccessor,
    private val abiservice: ABIResource
) : UserPoolingService {

    val erc20ABI by lazy {
        abiservice.getABI("general/ERC20.json")
    }

    @Cacheable(cacheNames = ["quickswap-lps"], key = "#address")
    override fun userPoolings(address: String): List<PoolingElement> {
        val tokens = quickswapService.getPairs()

        return polygonContractAccessor.readMultiCall(
            tokens.map { token ->
                MultiCallElement(
                    polygonContractAccessor.createFunction(
                        polygonContractAccessor.getFunction(
                            erc20ABI, "balanceOf"
                        )!!,
                        listOf(address.toAddress()),
                        listOf(
                            TypeReference.create(Uint256::class.java)
                        )
                    ),
                    token.id
                )
            }
        ).mapIndexed { index, item ->


            val want = tokens[index]
            val balance = item[0].value as BigInteger

            if (balance > BigInteger.ZERO) {
                val token1 = want.token0
                val token2 = want.token1
                val amount = balance.toBigDecimal().divide(BigDecimal.TEN.pow(18), 4, RoundingMode.HALF_UP)
                PoolingElement(
                    lpAddress = want.id,
                    amount = amount,
                    name = "${token1.name}/${token2.name} LP",
                    network = getNetwork(),
                    protocol = Protocol.QUICKSWAP,
                    symbol = "${token1.symbol}/${token2.symbol}",
                    tokenType = TokenType.UNISWAP
                )
            } else {
                null
            }
        }.filterNotNull()
    }


    override fun getProtocol(): Protocol {
        return Protocol.QUICKSWAP;
    }

    override fun getNetwork(): Network {
        return Network.POLYGON;
    }
}