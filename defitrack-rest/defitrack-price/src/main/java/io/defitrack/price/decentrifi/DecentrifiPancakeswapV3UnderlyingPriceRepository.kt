package io.defitrack.price.decentrifi

import arrow.fx.coroutines.parMap
import io.defitrack.common.network.Network
import io.defitrack.common.utils.AsyncUtils.lazyAsync
import io.defitrack.common.utils.BigDecimalExtensions.dividePrecisely
import io.defitrack.erc20.TokenInformationVO
import io.defitrack.evm.contract.BlockchainGatewayProvider
import io.defitrack.market.pooling.vo.PoolingMarketVO
import io.defitrack.price.external.ExternalPrice
import io.defitrack.price.external.StablecoinPriceProvider
import io.defitrack.token.DecentrifiERC20Resource
import io.defitrack.uniswap.v3.UniswapV3PoolContract
import io.github.reactivecircus.cache4k.Cache
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.math.BigInteger
import java.util.concurrent.Executors
import kotlin.time.measureTime

@Component
class DecentrifiPancakeswapV3UnderlyingPriceRepository(
    private val httpClient: HttpClient,
    private val blockchainGatewayProvider: BlockchainGatewayProvider,
    private val erC20Resource: DecentrifiERC20Resource,
    private val stablecoinPriceProvider: StablecoinPriceProvider
) {

    private val logger = LoggerFactory.getLogger(this::class.java)
    val prices = Cache.Builder<String, ExternalPrice>().build()

    @Scheduled(fixedDelay = 1000 * 60 * 60 * 1) // every 24 hours
    fun populatePrices() {
        Executors.newSingleThreadExecutor().submit {
            runBlocking {
                val duration = measureTime {
                    val pools = getUniswapV3Pools()

                    try {
                        importUsdPairs(pools)
                        importEthPairs(pools)
                    } catch (e: Exception) {
                        logger.error("Unable to fetch pools for Pancakeswap V3, result was ${e.message}")
                    }
                }
                logger.info("[took ${duration.inWholeSeconds} seconds] pancakeswap V3 Underlying Price Repository populated with ${prices.asMap().entries.size} prices")
            }
        }
    }

    private suspend fun importEthPairs(pools: List<PoolingMarketVO>) {
        val eths = eths.await()
        val stables = stablecoinPriceProvider.stableCoins.await()

        val containsEth: (PoolingMarketVO) -> Boolean = {
            it.breakdown?.any { share ->
                (eths.get(it.network.toNetwork())?.address?.lowercase() == share.token.address.lowercase())
            } ?: false
        }

        val containsNoStables: (PoolingMarketVO) -> Boolean = {
            it.breakdown?.none { share ->
                stables.getOrDefault(it.network.toNetwork(), emptyList()).map { it.address.lowercase() }
                    .contains(share.token.address.lowercase())
            } ?: false
        }


        val ethPairs = pools.filter(containsEth).filter(containsEth).filter(containsNoStables)

        ethPairs.parMap(concurrency = 12) { pool ->
            try {

                val ethForNetwork = eths[pool.network.toNetwork()]?.address?.lowercase()

                if (ethForNetwork == null) {
                    logger.error("no known eth address found for network ${pool.network.name}")
                } else {

                    val ethPriceForNetwork =
                        prices.get(toIndex(pool.network.toNetwork(), ethForNetwork))?.price ?: BigDecimal.ZERO

                    if (ethPriceForNetwork == BigDecimal.ZERO) {
                        logger.error("price for eth was not prepopulated for network ${pool.network}")
                    }

                    val contract = UniswapV3PoolContract(
                        blockchainGatewayProvider.getGateway(pool.network.toNetwork()),
                        pool.address
                    )

                    val sqrtPriceX96 = contract.slot0().sqrtPriceX96
                    val token0 = erC20Resource.getTokenInformation(pool.network.toNetwork(), contract.token0.await())
                    val token1 = erC20Resource.getTokenInformation(pool.network.toNetwork(), contract.token1.await())

                    val priceInOtherToken = (sqrtPriceX96.toBigDecimal().dividePrecisely(
                        BigInteger.TWO.pow(96).toBigDecimal()
                    )).pow(2)

                    if (ethForNetwork == token0.address.lowercase()) {
                        val normalized = getNormalizedPrice(token0, token1, priceInOtherToken)
                        val price = ethPriceForNetwork.dividePrecisely(normalized)


                        val index = toIndex(pool.network.toNetwork(), token1.address)
                        if (prices.get(index) == null) {
                            prices.put(
                                index, ExternalPrice(
                                    token1.address, pool.network.toNetwork(), price, "pancakeswap-v3"
                                )
                            )
                        }
                    } else if (ethForNetwork == token1.address.lowercase()) {
                        val normalized = ethPriceForNetwork.times(getNormalizedPrice(token0, token1, priceInOtherToken))
                        val index = toIndex(pool.network.toNetwork(), token0.address)
                        if (prices.get(index) == null) {
                            prices.put(
                                index, ExternalPrice(
                                    token0.address, pool.network.toNetwork(), normalized, "pancakeswap-v3"
                                )
                            )
                        }
                    }
                }
            } catch (ex: Exception) {
                logger.error("Unable to fetch price for pool ${pool.address} on network ${pool.network}, result was ${ex.message}")
            }
        }
    }


    private suspend fun importUsdPairs(pools: List<PoolingMarketVO>) = coroutineScope {
        val stables = stablecoinPriceProvider.stableCoins.await()

        val containsStableCoin: (PoolingMarketVO) -> Boolean = {
            it.breakdown?.any { share ->
                stables.getOrDefault(it.network.toNetwork(), emptyList()).map { it.address.lowercase() }
                    .contains(share.token.address.lowercase())
            } ?: false
        }

        val usdPairs = pools.filter(containsStableCoin)

        val semaphore = Semaphore(12)

        usdPairs.forEach { pool ->
            launch {
                semaphore.withPermit {
                    try {

                        val stablesForNetwork = stables.getOrDefault(pool.network.toNetwork(), emptyList()).map {
                            it.address.lowercase()
                        }

                        val contract = UniswapV3PoolContract(
                            blockchainGatewayProvider.getGateway(pool.network.toNetwork()),
                            pool.address
                        )

                        val sqrtPriceX96 = contract.slot0().sqrtPriceX96
                        val token0 =
                            erC20Resource.getTokenInformation(pool.network.toNetwork(), contract.token0.await())
                        val token1 =
                            erC20Resource.getTokenInformation(pool.network.toNetwork(), contract.token1.await())

                        val priceInOtherToken = (sqrtPriceX96.toBigDecimal().dividePrecisely(
                            BigInteger.TWO.pow(96).toBigDecimal()
                        )).pow(2)

                        if (stablesForNetwork.contains(token0.address.lowercase())) {
                            val normalized = getNormalizedPrice(token0, token1, priceInOtherToken)
                            val price = BigDecimal.ONE.dividePrecisely(normalized)
                            prices.put(
                                toIndex(pool.network.toNetwork(), token1.address), ExternalPrice(
                                    token1.address, pool.network.toNetwork(), price, "uniswap-v3"
                                )
                            )
                        } else if (stablesForNetwork.contains(token1.address.lowercase())) {
                            val normalized = getNormalizedPrice(token0, token1, priceInOtherToken)
                            prices.put(
                                toIndex(pool.network.toNetwork(), token0.address), ExternalPrice(
                                    token0.address, pool.network.toNetwork(), normalized, "uniswap-v3"
                                )
                            )
                        }
                    } catch (ex: Exception) {
                        logger.error("Unable to fetch price for pool ${pool.address} on network ${pool.network}, result was ${ex.message}")
                    }
                }
            }
        }
    }

    private fun getNormalizedPrice(
        token0: TokenInformationVO,
        token1: TokenInformationVO,
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

    fun getPrice(tokenInformationVO: TokenInformationVO): BigDecimal? {
        return prices.get(toIndex(tokenInformationVO.network.toNetwork(), tokenInformationVO.address))?.price
    }

    fun contains(tokenInformationVO: TokenInformationVO): Boolean {
        return prices.asMap()
            .containsKey(toIndex(tokenInformationVO.network.toNetwork(), tokenInformationVO.address))
    }

    suspend fun getUniswapV3Pools(): List<PoolingMarketVO> = withContext(Dispatchers.IO) {
        val result = httpClient.get("https://api.decentri.fi/pancakeswap/pooling/all-markets")
        if (result.status.isSuccess()) result.body()
        else {
            logger.error("Unable to fetch pools for Pancakeswap V3, result was ${result.body<String>()}")
            emptyList()
        }
    }

    fun toIndex(network: Network, address: String): String {
        return network.name + "-" + address.lowercase()
    }

    val eths = lazyAsync {
        mapOf(
            Network.OPTIMISM to erC20Resource.getTokenInformation(
                Network.OPTIMISM,
                "0x4200000000000000000000000000000000000006"
            ),
            Network.BASE to erC20Resource.getTokenInformation(
                Network.BASE,
                "0x4200000000000000000000000000000000000006"
            ),
            Network.ETHEREUM to erC20Resource.getTokenInformation(
                Network.ETHEREUM,
                "0xc02aaa39b223fe8d0a0e5c4f27ead9083c756cc2"
            ),
            Network.ARBITRUM to erC20Resource.getTokenInformation(
                Network.ARBITRUM,
                "0x82af49447d8a07e3bd95bd0d56f35241523fbab1"
            )
        )
    }
}