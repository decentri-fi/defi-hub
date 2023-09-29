package io.defitrack.protocol.balancer.pooling

import com.google.gson.JsonParser
import io.defitrack.abi.TypeUtils
import io.defitrack.abi.TypeUtils.Companion.address
import io.defitrack.common.utils.FormatUtilsExtensions.asEth
import io.defitrack.common.utils.Refreshable
import io.defitrack.event.DefiEvent
import io.defitrack.event.DefiEventType
import io.defitrack.event.EventDecoder.Companion.getIndexedParameter
import io.defitrack.event.EventDecoder.Companion.getNonIndexedParameter
import io.defitrack.evm.contract.BlockchainGateway
import io.defitrack.evm.contract.GetEventLogsCommand
import io.defitrack.market.pooling.PoolingMarketProvider
import io.defitrack.market.pooling.domain.PoolingMarket
import io.defitrack.network.toVO
import io.defitrack.price.PriceRequest
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.balancer.contract.BalancerPoolContract
import io.defitrack.protocol.balancer.contract.BalancerPoolFactoryContract
import io.defitrack.protocol.balancer.contract.BalancerVaultContract
import io.defitrack.token.TokenType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.launch
import org.apache.commons.codec.binary.Hex
import org.web3j.abi.EventEncoder
import org.web3j.abi.FunctionReturnDecoder
import org.web3j.abi.TypeEncoder
import org.web3j.abi.TypeReference
import org.web3j.abi.datatypes.Address
import org.web3j.abi.datatypes.DynamicArray
import org.web3j.abi.datatypes.Event
import org.web3j.abi.datatypes.generated.Int256
import org.web3j.abi.datatypes.generated.Uint256
import java.math.BigDecimal
import java.math.BigInteger

abstract class BalancerPoolingMarketProvider(
    val factories: List<String>,
    val earliestBlock: String
) : PoolingMarketProvider() {

    override suspend fun produceMarkets(): Flow<PoolingMarket> = channelFlow {

        if (factories.isEmpty())
            return@channelFlow

        val logs = getBlockchainGateway().getEvents(
            GetEventLogsCommand(
                addresses = factories,
                topic = EventEncoder.encode(BalancerPoolFactoryContract.POOL_CREATED_EVENT),
                fromBlock = BigInteger(earliestBlock, 10),
            )
        )

        val pools = JsonParser.parseString(logs).asJsonObject["result"].asJsonArray.map {
            val createdPoolAddress = it.asJsonObject["topics"].asJsonArray[1].asString
            FunctionReturnDecoder.decodeIndexedValue(
                createdPoolAddress, address(true)
            ).value as String
        }

        pools.forEach { pool ->
            launch {
                throttled {
                    createMarket(pool)?.let {
                        send(it)
                    }
                }
            }
        }
    }

    private suspend fun createMarket(
        pool: String,
    ): PoolingMarket? {

        try {
            val poolContract = BalancerPoolContract(
                getBlockchainGateway(), pool
            )
            val vault = BalancerVaultContract(
                getBlockchainGateway(),
                poolContract.getVault()
            )

            val poolId = poolContract.getPoolId()

            val poolTokens = vault.getPoolTokens(poolId)
            val underlying = poolTokens.tokens.mapIndexed { index, it ->
                getToken(it) to poolTokens.balances[index]
            }.filter {
                it.first.address.lowercase() != pool.lowercase()
            }

            return create(
                identifier = poolId,
                address = pool,
                name = "${
                    underlying.joinToString("/") {
                        it.first.symbol
                    }
                } Pool",
                tokens = underlying.map {
                    it.first.toFungibleToken()
                },
                symbol = underlying.joinToString("/") {
                    it.first.symbol
                },
                apr = BigDecimal.ZERO,
                metadata = mapOf(
                    "poolId" to poolId,
                ),
                marketSize = Refreshable.refreshable {
                    val poolInfo = vault.getPoolTokens(poolId)

                    val tokens = poolInfo.tokens.mapIndexed { index, address ->
                        val token = getToken(address)
                        val balance = poolInfo.balances[index]
                        token to balance
                    }.filter {
                        it.first.address != pool
                    }


                    tokens.sumOf {
                        getPriceResource().calculatePrice(
                            PriceRequest(
                                it.first.address,
                                getNetwork(),
                                it.second.asEth(it.first.decimals)
                            )
                        )
                    }.toBigDecimal()
                },
                positionFetcher = defaultPositionFetcher(pool),
                totalSupply = Refreshable.refreshable {
                    getToken(pool).totalDecimalSupply()
                }

            )
        } catch (e: Exception) {
            logger.error("Error creating market for pool $pool", e)
            return null
        }
    }

    override fun getProtocol(): Protocol {
        return Protocol.BALANCER
    }


}