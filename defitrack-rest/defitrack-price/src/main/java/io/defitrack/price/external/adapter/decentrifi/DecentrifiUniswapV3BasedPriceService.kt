package io.defitrack.price.external.adapter.decentrifi

import arrow.fx.coroutines.parMap
import io.defitrack.adapter.output.domain.erc20.FungibleTokenInformation
import io.defitrack.adapter.output.domain.market.PoolingMarketInformationDTO
import io.defitrack.common.network.Network
import io.defitrack.common.utils.AsyncUtils
import io.defitrack.common.utils.BigDecimalExtensions.dividePrecisely
import io.defitrack.evm.contract.BlockchainGatewayProvider
import io.defitrack.evm.contract.BulkConstantResolver
import io.defitrack.port.output.ERC20Client
import io.defitrack.port.output.MarketClient
import io.defitrack.price.external.adapter.stable.StablecoinPriceProvider
import io.defitrack.price.external.domain.ExternalPrice
import io.defitrack.price.port.out.ExternalPriceService
import io.defitrack.protocol.Protocol
import io.defitrack.uniswap.v3.UniswapV3PoolContract
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import java.math.BigDecimal
import java.math.BigInteger
import kotlin.time.measureTime

abstract class DecentrifiUniswapV3BasedPriceService(
    private val blockchainGatewayProvider: BlockchainGatewayProvider,
    private val erC20ClientResource: ERC20Client,
    private val markets: MarketClient,
    private val stablecoinPriceProvider: StablecoinPriceProvider,
    private val bulkConstantResolver: BulkConstantResolver,
    private val protocol: Protocol
) : ExternalPriceService {

    override fun importOrder(): Int = 10

    override suspend fun getAllPrices(): Flow<ExternalPrice> = channelFlow {
        getPrices().forEach {
            send(it)
        }
    }

    private val logger = LoggerFactory.getLogger(this::class.java)
    private val prices = mutableListOf<ExternalPrice>()

    fun getPrices() = runBlocking {
        val duration = measureTime {
            val pools = getUniswapV3Pools()

            try {
                importUsdPairs(pools)
                importEthPairs(pools)
            } catch (e: Exception) {
                logger.error("Unable to fetch pools for ${protocol.name}, result was ${e.message}")
            }
        }
        logger.debug("[took ${duration.inWholeSeconds} seconds] ${protocol.name} Underlying Price Repository populated with ${prices.size} prices")
        prices
    }

    private suspend fun importEthPairs(pools: List<PoolingMarketInformationDTO>) {
        val eths = eths.await()
        val stables = stablecoinPriceProvider.stableCoins.await()

        val containsEth: (PoolingMarketInformationDTO) -> Boolean = {
            it.breakdown?.any { share ->
                (eths.get(it.network.toNetwork())?.address?.lowercase() == share.token.address.lowercase())
            } ?: false
        }

        val containsNoStables: (PoolingMarketInformationDTO) -> Boolean = {
            it.breakdown?.none { share ->
                stables.getOrDefault(it.network.toNetwork(), emptyList()).map { it.address.lowercase() }
                    .contains(share.token.address.lowercase())
            } ?: false
        }


        val ethPairs = pools.filter(containsEth).filter(containsNoStables).filter { market ->
            market.tokens.all {
                erC20ClientResource.getAllTokens(market.network.toNetwork(), true).map {
                    it.address.lowercase()
                }.contains(it.address.lowercase())
            }
        }

        ethPairs.zip(
            bulkConstantResolver.resolve(
                ethPairs.map { pool ->
                    with(blockchainGatewayProvider.getGateway(pool.network.toNetwork())) {
                        UniswapV3PoolContract(pool.address)
                    }
                }
            )
        ).parMap(concurrency = 12) { (pool, contract) ->
            try {

                val eth = eths[pool.network.toNetwork()]?.address?.lowercase()

                if (eth == null) {
                    logger.error("no known eth address found for network ${pool.network.name}")
                } else {

                    val ethPriceForNetwork =
                        prices.find {
                            it.address == eth
                        }?.price ?: BigDecimal.ZERO

                    if (ethPriceForNetwork == BigDecimal.ZERO) {
                        logger.error("price for eth was not prepopulated for network ${pool.network.name}")
                    }

                    val sqrtPriceX96 = contract.slot0.await().sqrtPriceX96
                    val token0 =
                        erC20ClientResource.getTokenInformation(pool.network.toNetwork(), contract.token0.await())
                    val token1 =
                        erC20ClientResource.getTokenInformation(pool.network.toNetwork(), contract.token1.await())

                    val priceInOtherToken = (sqrtPriceX96.toBigDecimal().dividePrecisely(
                        BigInteger.TWO.pow(96).toBigDecimal()
                    )).pow(2)

                    if (eth == token0.address.lowercase()) {
                        val normalized = getNormalizedPrice(token0, token1, priceInOtherToken)
                        val price = when {
                            normalized > BigDecimal.ZERO -> ethPriceForNetwork.dividePrecisely(normalized)
                            else -> BigDecimal.ZERO
                        }


                        addPrice(pool, token1, price)
                    } else if (eth == token1.address.lowercase()) {
                        val normalized =
                            ethPriceForNetwork.times(getNormalizedPrice(token0, token1, priceInOtherToken))
                        addPrice(pool, token0, normalized)
                    }
                }
            } catch (ex: Exception) {
                logger.error("Unable to fetch price for pool ${pool.address} on network ${pool.network}, result was ${ex.message}")
            }
        }
    }


    private suspend fun importUsdPairs(pools: List<PoolingMarketInformationDTO>) = coroutineScope {
        val stables = stablecoinPriceProvider.stableCoins.await()

        val containsStableCoin: (PoolingMarketInformationDTO) -> Boolean = {
            it.breakdown?.any { share ->
                stables.getOrDefault(it.network.toNetwork(), emptyList()).map { it.address.lowercase() }
                    .contains(share.token.address.lowercase())
            } ?: false
        }

        val usdPairs = pools.filter(containsStableCoin).filter { market ->
            market.tokens.all {
                erC20ClientResource.getAllTokens(market.network.toNetwork(), true).map {
                    it.address.lowercase()
                }.contains(it.address.lowercase())
            }
        }


        usdPairs.zip(
            bulkConstantResolver.resolve(
                usdPairs.map { pool ->
                    with(blockchainGatewayProvider.getGateway(pool.network.toNetwork())) {
                        UniswapV3PoolContract(
                            pool.address
                        )
                    }
                }
            )
        ).filter { (_, contract) ->
            contract.liquidity.await() > BigInteger.ZERO
        }.parMap(concurrency = 8) { (pool, contract) ->
            try {
                val stablesForNetwork = stables.getOrDefault(pool.network.toNetwork(), emptyList()).map {
                    it.address.lowercase()
                }


                val sqrtPriceX96 = contract.slot0.await().sqrtPriceX96
                val token0 =
                    erC20ClientResource.getTokenInformation(pool.network.toNetwork(), contract.token0.await())
                val token1 =
                    erC20ClientResource.getTokenInformation(pool.network.toNetwork(), contract.token1.await())

                val priceInOtherToken = (sqrtPriceX96.toBigDecimal().dividePrecisely(
                    BigInteger.TWO.pow(96).toBigDecimal()
                )).pow(2)

                if (stablesForNetwork.contains(token0.address.lowercase())) {
                    val normalized = getNormalizedPrice(token0, token1, priceInOtherToken)

                    val price = when {
                        normalized > BigDecimal.ZERO -> BigDecimal.ONE.dividePrecisely(normalized)
                        else -> BigDecimal.ZERO
                    }
                    logger.trace(
                        "found price for {} on network {} with price {}",
                        token1.symbol,
                        pool.network.name,
                        price
                    )
                    addPrice(pool, token1, price)
                } else if (stablesForNetwork.contains(token1.address.lowercase())) {
                    val normalized = getNormalizedPrice(token0, token1, priceInOtherToken)
                    logger.trace(
                        "found price for {} on network {} with price {}",
                        token0.symbol,
                        pool.network.name,
                        normalized
                    )
                    addPrice(pool, token0, normalized)
                }
            } catch (ex: Exception) {
                logger.error("Unable to fetch price for pool ${pool.address} on network ${pool.network}, result was ${ex.message}")
            }
        }
    }

    private fun addPrice(
        pool: PoolingMarketInformationDTO,
        token: FungibleTokenInformation,
        price: BigDecimal
    ) {
        prices.add(
            ExternalPrice(
                token.address, pool.network.toNetwork(), price, protocol.slug, pool.name, importOrder()
            )
        )
    }

    private fun getNormalizedPrice(
        token0: FungibleTokenInformation,
        token1: FungibleTokenInformation,
        priceInOtherToken: BigDecimal
    ) = if (token0.decimals >= token1.decimals) {
        priceInOtherToken.times(
            BigDecimal.TEN.pow(token0.decimals - token1.decimals)
        )
    } else {
        priceInOtherToken.dividePrecisely(
            BigDecimal.TEN.pow(token1.decimals - token0.decimals)
        )
    }

    suspend fun getUniswapV3Pools(): List<PoolingMarketInformationDTO> {
        return markets.getPoolingMarkets(protocol.slug)
    }

    val eths = AsyncUtils.lazyAsync {
        mapOf(
            Network.OPTIMISM to erC20ClientResource.getTokenInformation(
                Network.OPTIMISM,
                "0x4200000000000000000000000000000000000006"
            ),
            Network.BASE to erC20ClientResource.getTokenInformation(
                Network.BASE,
                "0x4200000000000000000000000000000000000006"
            ),
            Network.ETHEREUM to erC20ClientResource.getTokenInformation(
                Network.ETHEREUM,
                "0xc02aaa39b223fe8d0a0e5c4f27ead9083c756cc2"
            ),
            Network.ARBITRUM to erC20ClientResource.getTokenInformation(
                Network.ARBITRUM,
                "0x82af49447d8a07e3bd95bd0d56f35241523fbab1"
            )
        )
    }

}