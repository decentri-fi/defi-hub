package io.defitrack.protocol.uniswap.pooling

import io.defitrack.abi.ABIResource
import io.defitrack.common.network.Network
import io.defitrack.ethereum.config.EthereumContractAccessor
import io.defitrack.ethereumbased.contract.EvmContractAccessor.Companion.toAddress
import io.defitrack.ethereumbased.contract.multicall.MultiCallElement
import io.defitrack.pool.UserPoolingService
import io.defitrack.pool.domain.PoolingElement
import io.defitrack.protocol.Protocol
import io.defitrack.token.TokenType
import io.defitrack.uniswap.EthereumUniswapV2Service
import io.defitrack.uniswap.contract.UniswapLPTokenContract
import org.springframework.stereotype.Service
import org.web3j.abi.TypeReference
import org.web3j.abi.datatypes.generated.Uint256
import java.math.BigDecimal
import java.math.BigInteger
import java.math.RoundingMode

@Service
class UniswapUserPoolingService(
    private val etrhereumUniswapV2Service: EthereumUniswapV2Service,
    private val abiService: ABIResource,
    private val ethereumContractAccessor: EthereumContractAccessor,
) : UserPoolingService() {

    val erc20ABI by lazy {
        abiService.getABI("general/ERC20.json")
    }

    override suspend fun fetchUserPoolings(address: String): List<PoolingElement> {
        val allPairs = etrhereumUniswapV2Service.getPairs()
        return ethereumContractAccessor.readMultiCall(
            allPairs.map { token ->
                MultiCallElement(
                    ethereumContractAccessor.createFunction(
                        ethereumContractAccessor.getFunction(
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
                val token = UniswapLPTokenContract(
                    ethereumContractAccessor,
                    abiService.getABI("uniswap/UniswapV2Pair.json"),
                    address = want.id
                )

                val token1 = want.token0
                val token2 = want.token1
                val amount = balance.toBigDecimal().divide(BigDecimal.TEN.pow(token.decimals), 4, RoundingMode.HALF_UP)

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