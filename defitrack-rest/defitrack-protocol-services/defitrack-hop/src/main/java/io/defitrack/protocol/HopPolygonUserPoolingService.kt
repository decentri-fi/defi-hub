package io.defitrack.protocol

import io.defitrack.abi.ABIResource
import io.defitrack.common.network.Network
import io.defitrack.ethereumbased.contract.EvmContractAccessor.Companion.toAddress
import io.defitrack.ethereumbased.contract.multicall.MultiCallElement
import io.defitrack.polygon.config.PolygonContractAccessor
import io.defitrack.pool.UserPoolingService
import io.defitrack.pool.domain.PoolingElement
import io.defitrack.protocol.contract.HopLpTokenContract
import io.defitrack.protocol.staking.TokenType
import org.springframework.stereotype.Component
import org.web3j.abi.TypeReference
import org.web3j.abi.datatypes.generated.Uint256
import java.math.BigInteger

@Component
class HopPolygonUserPoolingService(
    private val hopPolygonPoolingMarketService: HopPolygonPoolingMarketService,
    private val polygonContractAccessor: PolygonContractAccessor,
    private val abiResource: ABIResource
) : UserPoolingService() {


    override suspend fun fetchUserPoolings(address: String): List<PoolingElement> {
        val markets = hopPolygonPoolingMarketService.fetchPoolingMarkets()

        return polygonContractAccessor.readMultiCall(markets.map {
            val contract = HopLpTokenContract(
                evmContractAccessor = polygonContractAccessor,
                abiResource.getABI("hop/SaddleToken.json"),
                it.address
            )
            MultiCallElement(
                contract.createFunction(
                    "balanceOf",
                    listOf(address.toAddress()),
                    listOf(
                        TypeReference.create(Uint256::class.java)
                    )
                ),
                it.address
            )
        }).mapIndexed { index, result ->
            val balance = result[0].value as BigInteger
            if (balance > BigInteger.ONE) {
                val pool = markets[index]
                PoolingElement(
                    lpAddress = pool.address,
                    amount = balance.toBigDecimal(),
                    name = pool.token.map { it.symbol }.joinToString { "/" } + " LP",
                    symbol = pool.token.map { it.symbol }.joinToString { "-" },
                    network = getNetwork(),
                    protocol = getProtocol(),
                    TokenType.HOP,
                    pool.id
                )
            } else {
                null
            }
        }.filterNotNull()
    }

    override fun getProtocol(): Protocol {
        return Protocol.HOP
    }

    override fun getNetwork(): Network {
        return Network.POLYGON
    }
}