package io.defitrack.protocol.application.baseswap

import arrow.core.Either
import arrow.core.nel
import arrow.fx.coroutines.parMap
import io.defitrack.abi.TypeUtils
import io.defitrack.abi.TypeUtils.Companion.uint256
import io.defitrack.architecture.conditional.ConditionalOnCompany
import io.defitrack.common.network.Network
import io.defitrack.common.utils.AsyncUtils
import io.defitrack.common.utils.refreshable
import io.defitrack.event.EventDecoder.Companion.extract
import io.defitrack.evm.GetEventLogsCommand
import io.defitrack.market.domain.PoolingMarket
import io.defitrack.market.port.out.PoolingMarketProvider
import io.defitrack.protocol.Company
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.baseswap.PoolingContract
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import org.springframework.stereotype.Component
import org.web3j.abi.EventEncoder
import org.web3j.abi.datatypes.Event
import java.math.BigInteger

@Component
@ConditionalOnCompany(Company.BASESWAP)
class BaseSwapPoolingMarketProvider : PoolingMarketProvider() {

    val event = Event(
        "PairCreated",
        listOf(
            TypeUtils.address(true),
            TypeUtils.address(true),
            TypeUtils.address(false),
            uint256()
        )
    )

    override suspend fun produceMarkets(): Flow<PoolingMarket> = channelFlow {
        getPools().map {
            createContract { PoolingContract(it) }
        }.resolve().parMap(concurrency = 12) { address ->
            Either.catch {
                createMarket(address)
            }.mapLeft {
                logger.error("Error creating market for $address: {}", it.message)
            }.getOrNull()
        }.filterNotNull()
            .forEach {
                send(it)
            }
    }

    val allVerifiedTokens = AsyncUtils.lazyAsync {
        getERC20Resource().getAllTokens(getNetwork(), true).map {
            it.address.lowercase()
        }
    }

    private suspend fun createMarket(poolAddress: PoolingContract): PoolingMarket {
        val token0 = getToken(poolAddress.token0.await())
        val token1 = getToken(poolAddress.token1.await())



        return create(
            name = "${token0.symbol}/${token1.symbol}",
            symbol = "${token0.symbol}-${token1.symbol}",
            breakdown = refreshable {
                breakdownOf(
                    poolAddress.address,
                    token0, token1
                )
            },
            address = poolAddress.address,
            identifier = poolAddress.address,
            totalSupply = refreshable {
                getToken(poolAddress.address).totalDecimalSupply()
            }
        )
    }

    private suspend fun getPools(): List<String> {
        val logs = getBlockchainGateway().getEventsAsEthLog(
            GetEventLogsCommand(
                "0xFDa619b6d20975be80A10332cD39b9a4b0FAa8BB".nel(),
                EventEncoder.encode(event),
                fromBlock = BigInteger.valueOf(2061422)

            )
        )

        return logs.map {
            event.extract<String>(it, false, 0)
        }
    }

    override fun getProtocol(): Protocol {
        return Protocol.BASESWAP
    }

    override fun getNetwork(): Network {
        return Network.BASE
    }
}