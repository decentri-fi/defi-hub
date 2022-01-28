package io.defitrack.protocol.dfyn.pooling

import io.defitrack.pool.UserPoolingService
import io.defitrack.pool.contract.LPTokenContract
import io.defitrack.pool.domain.PoolingElement
import io.defitrack.abi.ABIResource
import io.defitrack.common.network.Network
import io.defitrack.ethereumbased.contract.EvmContractAccessor.Companion.toAddress
import io.defitrack.ethereumbased.contract.multicall.MultiCallElement
import io.defitrack.polygon.config.PolygonContractAccessor
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.dfyn.DfynService
import io.defitrack.protocol.staking.TokenType
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import org.web3j.abi.TypeReference
import org.web3j.abi.datatypes.generated.Uint256
import java.math.BigDecimal
import java.math.BigInteger
import java.math.RoundingMode

@Service
class DfynUserPoolingService(
    private val uniswapService: DfynService,
    private val abiService: ABIResource,
    private val polygonContractAccessor: PolygonContractAccessor,
) : UserPoolingService() {

    val erc20ABI = abiService.getABI("general/ERC20.json")

    override suspend fun fetchUserPoolings(address: String): List<PoolingElement> {
        val allPairs = uniswapService.getPairs()
        return polygonContractAccessor.readMultiCall(
            allPairs.map { token ->
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

            val want = allPairs[index]
            val balance = item[0].value as BigInteger

            if (balance > BigInteger.ZERO) {
                val token = LPTokenContract(
                    polygonContractAccessor,
                    abiService.getABI("uniswap/UniswapV2Pair.json"),
                    address = want.id
                )

                val token1 = want.token0
                val token2 = want.token1
                val amount = balance.toBigDecimal().divide(BigDecimal.TEN.pow(token.decimals), 4, RoundingMode.HALF_UP)

                PoolingElement(
                    lpAddress = token.address,
                    amount = amount,
                    name = "${token1.name}/${token2.name} LP",
                    network = getNetwork(),
                    protocol = getProtocol(),
                    symbol = "${token1.symbol}/${token2.symbol}",
                    tokenType = TokenType.UNISWAP,
                    id = "dfyn-polygon-${want.id}",
                    )
            } else {
                null
            }
        }.filterNotNull()
    }

    override fun getProtocol(): Protocol {
        return Protocol.DFYN
    }

    override fun getNetwork(): Network {
        return Network.POLYGON
    }
}